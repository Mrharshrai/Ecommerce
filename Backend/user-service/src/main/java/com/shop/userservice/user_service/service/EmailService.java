package com.shop.userservice.user_service.service;

public interface EmailService {
    
    void sendOTPEmail(String toEmail, String otp, String purpose);
    
    void sendVerificationEmail(String toEmail, String otp);
    
    void sendEmail(String toEmail, String subject, String body);
}
