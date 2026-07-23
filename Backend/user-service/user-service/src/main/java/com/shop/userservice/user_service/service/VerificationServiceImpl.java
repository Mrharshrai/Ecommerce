package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.OTP;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.exception.InvalidCredentialsException;
import com.shop.userservice.user_service.exception.RateLimitException;
import com.shop.userservice.user_service.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.LockedException;

@Service
public class VerificationServiceImpl implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

    private static final String EMAIL_OTP_SUCCESS =
            "If an account is associated with this email, you will receive an OTP shortly.";
    private static final String MOBILE_OTP_SUCCESS =
            "If an account is associated with this mobile number, you will receive an OTP shortly.";
    private static final String PASSWORD_RESET_SUCCESS =
            "Password reset successfully. Please log in with your new password.";
    private static final String GENERIC_ERROR =
            "An error occurred. Please try again later.";
    private static final String UNEXPECTED_ERROR =
            "An unexpected error occurred. Please try again later.";

    private final UserService userService;
    private final OTPService otpService;
    private final EmailService emailService;
    private final SmsService smsService;

    public VerificationServiceImpl(UserService userService,
                                   OTPService otpService,
                                   EmailService emailService,
                                   SmsService smsService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    // -------------------------------------------------------------------------
    // Forgot-password OTP dispatch
    // -------------------------------------------------------------------------

    /**
     * Looks up an active user by email, generates a FORGOT_PASSWORD OTP,
     * and sends it via email. Returns a generic message to prevent user enumeration.
     */
    @Override
    public ResponseEntity<String> sendForgotPasswordOtpByEmail(String email) {
        log.debug("Processing forgot-password OTP request for email: {}", email);
        try {
            userService.findActiveUserByEmail(email);
            log.debug("Active user confirmed for email: {}", email);

            String otp = otpService.generateAndSaveOTP(email, OTP.OTPPurpose.FORGOT_PASSWORD);
            log.debug("FORGOT_PASSWORD OTP generated for email: {}", email);

            emailService.sendOTPEmail(email, otp, "Password Reset");
            log.info("Password-reset OTP dispatched via email to: {}", email);

            return ResponseEntity.ok(EMAIL_OTP_SUCCESS);

        } catch (RateLimitException e) {
            log.warn("Rate limit exceeded for forgot-password email: {}", email);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());

        } catch (InvalidCredentialsException e) {
            // User not found — return generic message to prevent enumeration
            log.info("Forgot-password requested for unrecognised email (silenced): {}", email);
            return ResponseEntity.ok(EMAIL_OTP_SUCCESS);

        } catch (LockedException e) {
            log.warn("Forgot password requested for locked account: {}", email);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error dispatching forgot-password OTP to email {}: {}",
                    email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GENERIC_ERROR);
        }
    }

    /**
     * Looks up an active user by mobile, generates a FORGOT_PASSWORD OTP,
     * and sends it via SMS. Returns a generic message to prevent user enumeration.
     */
    @Override
    public ResponseEntity<String> sendForgotPasswordOtpByMobile(String mobile) {
        log.debug("Processing forgot-password OTP request for mobile: {}", mobile);
        try {
            userService.findActiveUserByMobile(mobile);
            log.debug("Active user confirmed for mobile: {}", mobile);

            String otp = otpService.generateAndSaveOTP(mobile, OTP.OTPPurpose.FORGOT_PASSWORD);
            log.debug("FORGOT_PASSWORD OTP generated for mobile: {}", mobile);

            smsService.sendOTPSMS(mobile, otp, "Password Reset");
            log.info("Password-reset OTP dispatched via SMS to mobile: {}", mobile);

            return ResponseEntity.ok(MOBILE_OTP_SUCCESS);

        } catch (RateLimitException e) {
            log.warn("Rate limit exceeded for forgot-password mobile: {}", mobile);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());

        } catch (InvalidCredentialsException e) {
            // User not found — return generic message to prevent enumeration
            log.info("Forgot-password requested for unrecognised mobile (silenced): {}", mobile);
            return ResponseEntity.ok(MOBILE_OTP_SUCCESS);

        } catch (LockedException e) {
            log.warn("Forgot password requested for locked account: {}", mobile);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error dispatching forgot-password OTP to mobile {}: {}",
                    mobile, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GENERIC_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // Reset-password (OTP verification + password update)
    // -------------------------------------------------------------------------

    /**
     * Verifies the FORGOT_PASSWORD OTP for the given email and,
     * if valid, updates the user's password.
     */
    @Override
    public ResponseEntity<String> resetPasswordByEmail(String email, String otp, String newPassword) {
        log.debug("Attempting password reset for email: {}", email);
        try {
            boolean isOtpValid = otpService.verifyOTP(email, otp, OTP.OTPPurpose.FORGOT_PASSWORD);
            if (!isOtpValid) {
                log.warn("Invalid or expired OTP submitted for email: {}", email);
                return ResponseEntity.badRequest().body("Invalid or expired OTP.");
            }

            userService.resetPassword(email, newPassword);
            log.info("Password reset successfully for email: {}", email);

            return ResponseEntity.ok(PASSWORD_RESET_SUCCESS);

        } catch (InvalidCredentialsException e) {
            // Covers "too many failed OTP attempts"
            log.warn("Too many failed OTP attempts for email: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (ResourceNotFoundException e) {
            // User disappeared between OTP request and reset
            log.warn("User account no longer active for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User account no longer active.");

        } catch (Exception e) {
            log.error("Unexpected error during password reset for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UNEXPECTED_ERROR);
        }
    }

    /**
     * Looks up the user by mobile, verifies the FORGOT_PASSWORD OTP,
     * and updates the user's password via their email address.
     */
    @Override
    public ResponseEntity<String> resetPasswordByMobile(String mobile, String otp, String newPassword) {
        log.debug("Attempting password reset for mobile: {}", mobile);
        try {
            User user = userService.findActiveUserByMobile(mobile);
            log.debug("Active user found for mobile {}: {}", mobile, user.getEmail());

            boolean isOtpValid = otpService.verifyOTP(mobile, otp, OTP.OTPPurpose.FORGOT_PASSWORD);
            if (!isOtpValid) {
                log.warn("Invalid or expired OTP submitted for mobile: {}", mobile);
                return ResponseEntity.badRequest().body("Invalid or expired OTP.");
            }
            log.debug("OTP verified for mobile: {}", mobile);

            // Reset using the user's email (that's how UserService identifies accounts)
            userService.resetPassword(user.getEmail(), newPassword);
            log.info("Password reset successfully for user with mobile: {}", mobile);

            return ResponseEntity.ok(PASSWORD_RESET_SUCCESS);

        } catch (InvalidCredentialsException e) {
            log.warn("Too many failed OTP attempts for mobile: {}", mobile);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (ResourceNotFoundException e) {
            log.warn("User account no longer active for mobile: {}", mobile);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User account no longer active.");

        } catch (LockedException e) {
            log.warn("Reset password requested for locked account: {}", mobile);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error during password reset for mobile {}: {}", mobile, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UNEXPECTED_ERROR);
        }
    }
}
