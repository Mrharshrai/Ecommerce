package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.OTP;

public interface OTPService {
    
    String generateAndSaveOTP(String identifier, OTP.OTPPurpose purpose);
    
    boolean verifyOTP(String identifier, String otp, OTP.OTPPurpose purpose);

    /**
     * Verifies the OTP but does NOT delete it on success.
     * Marks it as verified=true so the registration step can confirm pre-verification.
     * Used exclusively for PRE_REG_EMAIL / PRE_REG_MOBILE purposes.
     */
    boolean verifyAndKeepOTP(String identifier, String otp, OTP.OTPPurpose purpose);

    void cleanupExpiredOTPs();
}
