package com.shop.userservice.user_service.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DriverRegistrationDTO {

    // Basic user info
    @NotBlank(message = "name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._%+-]*@meradesh\\.com$",
            message = "Driver email must start with a letter and use @meradesh.com domain"
    )
    private String email;

    @NotBlank(message = "password is required")
    @Pattern(
            regexp = "^.{8,}$",
            message = "password must be at least 8 characters"
    )
    private String password;

    // Driver-specific info
    @NotBlank(message = "phoneNumber is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}\\d{10}$",
            message = "Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)"
    )
    private String phoneNumber;

    @NotBlank(message = "vehicleNumber is required")
    @Pattern(
            regexp = "^[A-Za-z]{2}[0-9]{2}[A-Za-z]{2}[0-9]{4}$",
            message = "Invalid vehicle number format. Example: HR26AA0110"
    )
    private String vehicleNumber;

    public DriverRegistrationDTO() {
    }

    public DriverRegistrationDTO(String name, String email, String password, String phoneNumber, String vehicleNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.vehicleNumber = vehicleNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}
