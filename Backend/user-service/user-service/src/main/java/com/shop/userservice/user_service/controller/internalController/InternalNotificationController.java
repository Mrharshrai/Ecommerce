package com.shop.userservice.user_service.controller.internalController;

import com.shop.userservice.user_service.dto.requestDTO.EmailRequestDTO;
import com.shop.userservice.user_service.dto.requestDTO.SmsRequestDTO;
import com.shop.userservice.user_service.service.EmailService;
import com.shop.userservice.user_service.service.SmsService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@Hidden
public class InternalNotificationController {

    private final EmailService emailService;
    private final SmsService smsService;

    public InternalNotificationController(EmailService emailService, SmsService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(@RequestBody EmailRequestDTO request) {
        try {
            emailService.sendEmail(request.getToEmail(), request.getSubject(), request.getBody());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sms")
    public ResponseEntity<Void> sendSms(@RequestBody SmsRequestDTO request) {
        try {
            smsService.sendSMS(request.getMobile(), request.getText());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
