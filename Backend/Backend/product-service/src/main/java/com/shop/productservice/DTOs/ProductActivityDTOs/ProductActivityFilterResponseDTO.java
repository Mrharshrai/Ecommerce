package com.shop.productservice.DTOs.ProductActivityDTOs;


import com.shop.productservice.enums.ProductActivityType;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductActivityFilterResponseDTO {

    // Available Admins
    private List<String> adminEmails;

    // Available Roles
    private List<String> userRoles;

    // Available Activity Types
    private List<ProductActivityType> activityTypes;

    // Available Date Range
    private Instant oldestActivity;

    private Instant latestActivity;

    // Total Records
    private Long totalActivities;
}
