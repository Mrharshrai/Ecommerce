package com.shop.userservice.user_service.service;


public interface SmsService {
    void sendOTPSMS(String mobile, String otp, String purpose);
    
    void sendSMS(String mobile, String text);
}