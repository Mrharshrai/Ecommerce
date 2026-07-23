package com.shop.productservice.config;

import com.shop.productservice.security.InternalJwtFilter;
import com.shop.productservice.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private InternalJwtFilter internalJwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Internal microservice communication
                        .requestMatchers("/internal/**").hasAuthority("INTERNAL")

                        // 2. Admin Endpoints
                        .requestMatchers("/api/adminProduct/**", "/api/adminRelatedProduct/**").hasRole("ADMIN")

                        // 3. Customer Authenticated Endpoints (FIXED)
                        .requestMatchers("/api/customer/reviews", "/api/customer/reviews/my-reviews").hasRole("CUSTOMER")
                        // Note: Use .authenticated() instead of .hasRole("CUSTOMER") if any logged-in user can access them.

                        // 4. Public Customer Endpoints
                        .anyRequest().permitAll()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // InternalJwtFilter runs first — only activates for /internal/** paths
                // JwtAuthFilter skips /internal/** (shouldNotFilter), so no overlap
                .addFilterBefore(internalJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
