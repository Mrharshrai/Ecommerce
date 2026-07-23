package com.shop.productservice.DTOs.ProductActivityDTOs;

import com.shop.productservice.enums.ProductActivityType;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductActivityResponseDTO {

    // Activity Log ID
    private Long id;

    // Admin Details
    private String userEmail;
    private String userRole;

    // Product ID / Discount ID
    private Long targetId;

    // Activity
    private ProductActivityType activityType;

    // Human-readable description
    private String description;

    // Request Details
    private String ipAddress;
    private String userAgent;

    // Activity Time
    private Instant timestamp;
}
