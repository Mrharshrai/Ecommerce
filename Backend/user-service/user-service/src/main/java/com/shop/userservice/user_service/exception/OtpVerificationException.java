package com.shop.userservice.user_service.exception;

/**
 * Thrown when a required OTP pre-verification step has not been completed.
 * Maps to HTTP 400 Bad Request via GlobalExceptionHandler.
 */
public class OtpVerificationException extends RuntimeException {

    public OtpVerificationException(String message) {
        super(message);
    }
}
