package com.shop.userservice.user_service.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerRegistrationDTO {
    // Basic user info
    @NotBlank(message = "name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9._%+-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    @Pattern(
            regexp = "^(?!.*@meradesh\\.com$).*$",
            message = "Customer email cannot use @meradesh.com domain"
    )
    private String email;

    @NotBlank(message = "password is required")
    @Pattern(
            regexp = "^.{8,}$",
            message = "password must be at least 8 characters"
    )
    private String password;

    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}\\d{10}$",
            message = "Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)"
    )
    private String mobile;

    public CustomerRegistrationDTO() {
    }

    public CustomerRegistrationDTO(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
