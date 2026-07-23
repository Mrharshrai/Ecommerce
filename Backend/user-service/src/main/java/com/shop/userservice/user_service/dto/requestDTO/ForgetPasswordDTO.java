package com.shop.userservice.user_service.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ForgetPasswordDTO {
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}\\d{10}$",
            message = "Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)"
    )
    private String mobile;


    public ForgetPasswordDTO(String email) {
        this.email = email;
    }

    public ForgetPasswordDTO() {

    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
