package com.shop.userservice.user_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SMSServiceIMPL implements SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public SMSServiceIMPL() {
        // default constructor
    }

    @Override
    public void sendOTPSMS(String mobile, String otp, String purpose) {
        // Format to E.164 if missing country code
        if (mobile != null && !mobile.startsWith("+")) {
            mobile = "+91" + mobile;
        }
        
        // Initialize Twilio
        Twilio.init(accountSid, authToken);
        String messageBody = "Your OTP for Mobile:- " + purpose + " is: " + otp;
        try {
            Message message = Message.creator(
                    new PhoneNumber(mobile),
                    new PhoneNumber(fromPhoneNumber),
                    "Your OTP for " + purpose + " is: " + otp
            ).create();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending SMS: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendSMS(String mobile, String text) {
        // Format to E.164 if missing country code
        if (mobile != null && !mobile.startsWith("+")) {
            mobile = "+91" + mobile;
        }

        Twilio.init(accountSid, authToken);
        try {
            Message message = Message.creator(
                    new PhoneNumber(mobile),
                    new PhoneNumber(fromPhoneNumber),
                    text
            ).create();
            System.out.println("SMS sent successfully to " + mobile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending SMS to " + mobile + ": " + e.getMessage());
        }
    }
}
