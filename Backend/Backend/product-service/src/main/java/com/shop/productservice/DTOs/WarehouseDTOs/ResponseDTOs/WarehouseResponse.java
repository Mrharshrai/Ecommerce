package com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {

    private Long id;
    private String warehouseCode;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String contactNumber;
    private String email;
    private boolean isActive;
    private boolean isDefault;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;
}
