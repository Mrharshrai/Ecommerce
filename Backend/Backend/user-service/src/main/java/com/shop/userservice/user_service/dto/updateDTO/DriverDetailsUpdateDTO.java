package com.shop.userservice.user_service.dto.updateDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverDetailsUpdateDTO {

    private String name;

    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._%+-]*@meradesh\\.com$",
            message = "Driver email must start with a letter and use @meradesh.com domain"
    )
    private String email;

    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}\\d{10}$",
            message = "Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)"
    )
    private String phoneNumber;

    @Pattern(
            regexp = "^[A-Za-z]{2}[0-9]{2}[A-Za-z]{2}[0-9]{4}$",
            message = "Invalid vehicle number format. Example: HR26AA0110"
    )
    private String vehicleNumber;
}

