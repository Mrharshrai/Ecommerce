package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.OTP;
import com.shop.userservice.user_service.exception.InvalidCredentialsException;
import com.shop.userservice.user_service.exception.RateLimitException;
import com.shop.userservice.user_service.repository.OTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class OTPServiceImpl implements OTPService {

    private static final Logger log = LoggerFactory.getLogger(OTPServiceImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OTPRepository otpRepository;
    
    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;
    
    @Value("${otp.length}")
    private int otpLength;

    public OTPServiceImpl(OTPRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Override
    @Transactional
    public String generateAndSaveOTP(String identifier, OTP.OTPPurpose purpose) {

        // 1. Use Instant for precise UTC timing
        Instant now = Instant.now();

        // Check last 60 seconds
        if (otpRepository.existsByIdentifierAndPurposeAndCreatedAtAfter(
                identifier,
                purpose,
                now.minus(1, ChronoUnit.MINUTES))) {
            throw new RateLimitException("Please wait 60 seconds before requesting another OTP.");
        }

        String otp = generateRandomOTP();

        // 2. Set expiry exactly 5 minutes from "now"
        Instant expiryTime = now.plus(otpExpiryMinutes, ChronoUnit.MINUTES);

        // 3. Invalidate old ones
        otpRepository.deleteByIdentifierAndPurpose(identifier, purpose);

        OTP otpEntity = OTP.builder()
                .identifier(identifier)
                .otp(otp)
                .purpose(purpose)
                .expiryTime(expiryTime)
                .verified(false)
                .build();
        otpRepository.save(otpEntity);
        log.info("OTP generated for {} with purpose: {}", identifier, purpose);
        return otp;
    }

    @Override
    @Transactional
    public boolean verifyOTP(String identifier, String otp, OTP.OTPPurpose purpose) {

        // 1. Use Instant.now() to match the UTC storage in DB
        Instant now = Instant.now();

        var otpEntityOpt = otpRepository.findByIdentifierAndPurpose(identifier, purpose);

        if (otpEntityOpt.isEmpty()) {
            log.warn("OTP verification failed: OTP not found for {}", identifier);
            return false;
        }

        OTP otpEntity = otpEntityOpt.get();

        // 2. Expiry Check (Instant comparison is very efficient)
        if (otpEntity.getExpiryTime().isBefore(now)) {
            otpRepository.delete(otpEntity);
            log.warn("OTP verification failed: OTP expired for {}", identifier);
            return false;
        }

        // 3. Check if the OTP is incorrect
        if (!otpEntity.getOtp().equals(otp)) {
            otpEntity.setFailedAttempts(otpEntity.getFailedAttempts() + 1);

            if (otpEntity.getFailedAttempts() >= 5) {
                otpRepository.deleteByIdentifierAndPurpose(identifier, purpose);
                log.warn("OTP for {} deleted due to too many failed attempts.", identifier);
                throw new InvalidCredentialsException("Too many failed attempts. Please request a new OTP.");
            }
            otpRepository.save(otpEntity);
            return false;
        }

        // 4. Success Logic
        otpEntity.setVerified(true);

        // Delete immediately to prevent reuse (Standard Security Practice)
        otpRepository.delete(otpEntity);
        log.info("OTP verified successfully for {}", identifier);
        return true;
    }

    @Override
    @Transactional
    public boolean verifyAndKeepOTP(String identifier, String otp, OTP.OTPPurpose purpose) {

        // 1. Unified UTC Time
        Instant now = Instant.now();

        var otpEntityOpt = otpRepository.findByIdentifierAndPurpose(identifier, purpose);

        if (otpEntityOpt.isEmpty()) {
            log.warn("verifyAndKeepOTP: OTP not found for {}", identifier);
            return false;
        }

        OTP otpEntity = otpEntityOpt.get();

        // 2. Consistent UTC Expiry Check
        if (otpEntity.getExpiryTime().isBefore(now)) {
            otpRepository.delete(otpEntity);
            log.warn("verifyAndKeepOTP: OTP expired for {}", identifier);
            return false;
        }

        // 3. Failed Attempt Logic
        if (!otpEntity.getOtp().equals(otp)) {
            otpEntity.setFailedAttempts(otpEntity.getFailedAttempts() + 1);
            if (otpEntity.getFailedAttempts() >= 5) {
                otpRepository.deleteByIdentifierAndPurpose(identifier, purpose);
                log.warn("verifyAndKeepOTP: OTP for {} deleted due to too many failed attempts.", identifier);
                throw new InvalidCredentialsException("Too many failed attempts. Please request a new OTP.");
            }
            otpRepository.save(otpEntity);
            return false;
        }

        // 4. Verification Persistence
        // We keep the record so the final /register call can verify 'verified == true'
        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        log.info("verifyAndKeepOTP: OTP verified and kept for {} (purpose: {})", identifier, purpose);
        return true;
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC") // Run daily at 3 AM
    // sec - min - hour - Every day of month- Every month - Every day of the week.
    public void cleanupExpiredOTPs() {
        Instant now = Instant.now();
        otpRepository.deleteByExpiryTimeBefore(now);
        log.info("Cleaned up expired OTPs at UTC: {}", now);
    }

    private String generateRandomOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(SECURE_RANDOM.nextInt(10));
        }
        return otp.toString();
    }
}
