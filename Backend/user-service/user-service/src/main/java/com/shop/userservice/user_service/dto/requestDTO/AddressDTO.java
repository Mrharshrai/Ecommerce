package com.shop.userservice.user_service.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 must be under 100 characters")
    private String addressLine1;

    @Size(max = 100, message = "Address line 2 must be under 100 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    private Double latitude;
    private Double longitude;

}
