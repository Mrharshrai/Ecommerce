package com.shop.userservice.user_service;

import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
//        System.out.println(new BCryptPasswordEncoder().encode("admin123"));
    }

    // Add admin user creation here
//    @Bean
//    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            String adminEmail = "admin@meradesh.com";
//            String adminName = "Admin User";
//            String adminPassword = "admin123";
//            String roleName = "ADMIN";  // if your entity has a string field for roleName
//            Set<Role> roles = Collections.singleton(Role.ADMIN);
//
//            // 1. Use the Builder pattern to create the User object
//            User admin = User.builder()
//                    .name(adminName)
//                    .email(adminEmail)
//                    .password(passwordEncoder.encode(adminPassword))
////                    .roleName("ADMIN") // Set the string field
//                    .roles(roles)      // Set the element collection
//                    // Set initial status fields
//                    .active(true)
//                    .deleted(false)
//                    // Note: @CreationTimestamp and @UpdateTimestamp handle creation/update,
//                    // but setting them here is fine if you want immediate control.
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .build();
//
//            userRepository.save(admin);
//            admin.setUserCode("ADM" + String.format("%04d", admin.getId()));
//            userRepository.save(admin);
//            System.out.println(admin);
//            System.out.println("✅ Admin user created with email: " + adminEmail+adminPassword);
//        };
//    }

}
