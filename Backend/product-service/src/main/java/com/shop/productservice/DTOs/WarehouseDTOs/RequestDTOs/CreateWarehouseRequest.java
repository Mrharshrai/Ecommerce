package com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Warehouse code must be less than 50 characters")
    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be less than 100 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must be less than 10 characters")
    private String pincode;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @NotBlank(message = "Contact number is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}\\d{10}$",
            message = "Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)"
    )
    private String contactNumber;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9._%+-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;

    private Boolean isDefault;
}
