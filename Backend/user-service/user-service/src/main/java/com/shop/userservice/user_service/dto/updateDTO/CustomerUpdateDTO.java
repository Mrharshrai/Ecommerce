package com.shop.userservice.user_service.dto.updateDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerUpdateDTO {
    private String name;

    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9._%+-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    @Pattern(
            regexp = "^(?!.*@meradesh\\.com$).*$",
            message = "Customer email cannot use @meradesh.com domain"
    )
    private String email;

    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}\\d{10}$",
            message = "Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)"
    )
    private String mobile;
}

