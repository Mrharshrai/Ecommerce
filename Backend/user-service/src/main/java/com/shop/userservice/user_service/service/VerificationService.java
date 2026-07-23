package com.shop.userservice.user_service.service;

import org.springframework.http.ResponseEntity;

public interface VerificationService {

    /**
     * Validates the email, generates a FORGOT_PASSWORD OTP,
     * and dispatches it via email. Always returns a generic success
     * message to prevent user-enumeration.
     */
    ResponseEntity<String> sendForgotPasswordOtpByEmail(String email);

    /**
     * Validates the mobile number, generates a FORGOT_PASSWORD OTP,
     * and dispatches it via SMS. Always returns a generic success
     * message to prevent user-enumeration.
     */
    ResponseEntity<String> sendForgotPasswordOtpByMobile(String mobile);

    /**
     * Verifies the FORGOT_PASSWORD OTP for the given email and,
     * if valid, updates the user's password.
     */
    ResponseEntity<String> resetPasswordByEmail(String email, String otp, String newPassword);

    /**
     * Looks up the user by mobile number, verifies the FORGOT_PASSWORD OTP,
     * and updates the user's password via their email address.
     */
    ResponseEntity<String> resetPasswordByMobile(String mobile, String otp, String newPassword);
}

