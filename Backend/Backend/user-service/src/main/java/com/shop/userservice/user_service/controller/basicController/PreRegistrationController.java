package com.shop.userservice.user_service.controller.basicController;

import com.shop.userservice.user_service.entity.OTP;
import com.shop.userservice.user_service.exception.InvalidCredentialsException;
import com.shop.userservice.user_service.exception.RateLimitException;
import com.shop.userservice.user_service.repository.UserRepository;
import com.shop.userservice.user_service.service.EmailService;
import com.shop.userservice.user_service.service.OTPService;
import com.shop.userservice.user_service.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fully public endpoints (no JWT required) for verifying email and mobile
 * BEFORE a customer account is created.
 *
 * Flow:
 *   1. POST /api/auth/pre-register/send-email-otp       → sends OTP to email
 *   2. POST /api/auth/pre-register/verify-email-otp     → verifies OTP (keeps record, marks verified=true)
 *   3. POST /api/auth/pre-register/send-mobile-otp      → (optional) sends OTP to mobile
 *   4. POST /api/auth/pre-register/verify-mobile-otp    → (optional) verifies mobile OTP
 *   5. POST /api/users/register/customer                → creates account (checks verified OTPs)
 */
@RestController
@RequestMapping("/api/pre-register")
@Tag(name = "Pre-Registration", description = "Public OTP verification before customer registration")
public class PreRegistrationController {

    private final OTPService otpService;

    private final EmailService emailService;

    private final SmsService smsService;

    private final UserRepository userRepository;


    public PreRegistrationController(OTPService otpService, EmailService emailService,
                                     SmsService smsService, UserRepository userRepository) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.userRepository = userRepository;
    }

    // ─────────────────────────── EMAIL ───────────────────────────

    @PostMapping("/send-email-otp")
    @Operation(
        summary = "Send pre-registration email OTP",
        description = "Sends a 6-digit OTP to the given email. No account required. Rate-limited to 1 request per 60 seconds."
    )
    public ResponseEntity<String> sendEmailOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        // Block internal/reserved @meradesh domain from receiving OTPs
        String emailLower = email.toLowerCase();
        if (emailLower.contains("@meradesh.")) {
            return ResponseEntity.badRequest()
                    .body("Registration with a @meradesh email address is not allowed.");
        }

        // Reject if this email is already registered to an existing (non-deleted) account
        if (userRepository.findByEmailAndDeletedFalse(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("An account with this email already exists. Please login or use a different email.");
        }
        try {
            String otp = otpService.generateAndSaveOTP(email, OTP.OTPPurpose.PRE_REG_EMAIL);
            emailService.sendVerificationEmail(email, otp);
            return ResponseEntity.ok("OTP sent to " + email + ". Valid for 5 minutes.");
        } catch (RateLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send OTP. Please try again.");
        }
    }

    @PostMapping("/verify-email-otp")
    @Operation(
        summary = "Verify pre-registration email OTP",
        description = "Verifies the OTP sent to the email. On success, marks it as verified (kept in DB until registration completes)."
    )
    public ResponseEntity<String> verifyEmailOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest().body("Email and OTP are required.");
        }
        try {
            boolean valid = otpService.verifyAndKeepOTP(email, otp, OTP.OTPPurpose.PRE_REG_EMAIL);
            if (!valid) {
                return ResponseEntity.badRequest().body("Invalid or expired OTP.");
            }
            return ResponseEntity.ok("Email verified successfully. You may now complete registration.");
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OTP verification failed. Please try again.");
        }
    }

    // ─────────────────────────── MOBILE ──────────────────────────

    @PostMapping("/send-mobile-otp")
    @Operation(
        summary = "Send pre-registration mobile OTP",
        description = "Sends a 6-digit OTP to the given mobile number. Optional — only required when mobile is included in registration."
    )
    public ResponseEntity<String> sendMobileOtp(@RequestBody Map<String, String> body) {
        String mobile = body.get("mobile");
        if (mobile == null || mobile.isBlank()) {
            return ResponseEntity.badRequest().body("Mobile number is required.");
        }

        if (!mobile.matches("^\\+[1-9]\\d{0,2}\\d{10}$")) {
            return ResponseEntity.badRequest().body("Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)");
        }

        // Reject if this mobile is already registered to an existing account
        if (userRepository.existsByMobile(mobile)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("An account with this mobile number already exists. Please login or use a different number.");
        }

        try {
            // NOTE: generateAndSaveOTP re-uses the email service abstraction here.
            // Replace with an SMS service call when SMS integration is added.
            String otp = otpService.generateAndSaveOTP(mobile, OTP.OTPPurpose.PRE_REG_MOBILE);
            smsService.sendOTPSMS(mobile, otp, String.valueOf(OTP.OTPPurpose.PRE_REG_MOBILE)); // Placeholder for actual SMS sending
            return ResponseEntity.ok("OTP generated for mobile " + mobile + ". Valid for 5 minutes.");
        } catch (RateLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send mobile OTP. Please try again.");
        }
    }

    @PostMapping("/verify-mobile-otp")
    @Operation(
        summary = "Verify pre-registration mobile OTP",
        description = "Verifies the OTP for the mobile number. On success, marks it as verified until registration completes."
    )
    public ResponseEntity<String> verifyMobileOtp(@RequestBody Map<String, String> body) {
        String mobile = body.get("mobile");
        String otp    = body.get("otp");

        if (mobile == null || mobile.isBlank() || otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest().body("Mobile and OTP are required.");
        }
        try {
            boolean valid = otpService.verifyAndKeepOTP(mobile, otp, OTP.OTPPurpose.PRE_REG_MOBILE);
            if (!valid) {
                return ResponseEntity.badRequest().body("Invalid or expired OTP.");
            }
            return ResponseEntity.ok("Mobile verified successfully. You may now complete registration.");
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OTP verification failed. Please try again.");
        }
    }
}
