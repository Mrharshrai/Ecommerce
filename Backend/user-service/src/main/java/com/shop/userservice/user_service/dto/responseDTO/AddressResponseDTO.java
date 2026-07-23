package com.shop.userservice.user_service.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponseDTO {

    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}