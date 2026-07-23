package com.shop.userservice.user_service.config;

import com.shop.userservice.user_service.security.internal.InternalJwtFilter;
import com.shop.userservice.user_service.security.JwtFilter;
import com.shop.userservice.user_service.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    private final InternalJwtFilter internalJwtFilter;

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, InternalJwtFilter internalJwtFilter, UserDetailsServiceImpl userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.internalJwtFilter = internalJwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- 1. PUBLIC ENDPOINTS (No Token Required) ---
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/pre-register/**").permitAll()   // pre-registration OTP (no JWT)
                        .requestMatchers("/api/users/register/customer").permitAll()
                        .requestMatchers("/error/**", "/actuator/**").permitAll()

                        // one api for admin to get details by id in user controller

                        // --- 2. INTERNAL COMMUNICATION (Microservice Authority) ---
                        .requestMatchers("/internal/**").hasAnyAuthority("INTERNAL", "ROLE_INTERNAL")

                        // --- 3. ADMIN ONLY (Management & Analytics) ---
                        .requestMatchers("/api/analytics/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/all","/api/users/get/**", "/api/users/customers/**","/api/users/activate/**", "/api/users/deactivate/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/register/driver").hasRole("ADMIN")
                        .requestMatchers("/api/users/updateDriverDetails/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/softDeleteEmployee/**", "/api/users/restoreEmployee/**").hasRole("ADMIN")

                        // --- 4. CUSTOMER ONLY (Personal Data & Verification) ---
                        .requestMatchers("/api/addresses/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/users/updateCustomer/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/users/customer/deactivate").hasRole("CUSTOMER")

                        // --- 5. SHARED PROTECTED ENDPOINTS (Role-Specific @PreAuthorize in Controller) ---
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/users/profile").authenticated()
                        .requestMatchers("/api/users/updatePassword/**").hasAnyRole("ADMIN", "DRIVER","CUSTOMER")
                        .requestMatchers("/api/users/deleteAccount/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // --- 6. GLOBAL FALLBACK ---
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(internalJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
