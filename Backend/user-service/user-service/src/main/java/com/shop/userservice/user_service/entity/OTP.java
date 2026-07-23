package com.shop.userservice.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifier; // email or mobile

    @Column(nullable = false, length = 6)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OTPPurpose purpose;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;


    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant expiryTime;

    @Builder.Default // Ensure Builder uses the default value of 0
    @Column(nullable = false)
    private int failedAttempts = 0;

    public enum OTPPurpose {
        EMAIL_VERIFICATION,
        MOBILE_VERIFICATION,
        FORGOT_PASSWORD,
        RESET_PASSWORD,
        PRE_REG_EMAIL,   // pre-registration email OTP (public, no JWT)
        PRE_REG_MOBILE   // pre-registration mobile OTP (public, no JWT)
        }
}
