package com.shop.userservice.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String userCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    // -------------------------
    // Authentication & Verification
    // -------------------------

    @Column(unique = true)
    private String mobile;

    @Column(length = 500)
    private String refreshToken;

    @Column(columnDefinition = "TIMESTAMP(6)")
    private Instant refreshTokenExpiry;;

    @Column(nullable = false)
    @Builder.Default
    private boolean mobileVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    // -------------------------
    // Soft Delete & Status
    // -------------------------

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;      // if true → not visible in lists

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;        // can login or not

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt; // Changed from LocalDateTime

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt; // Changed from LocalDateTime
}
