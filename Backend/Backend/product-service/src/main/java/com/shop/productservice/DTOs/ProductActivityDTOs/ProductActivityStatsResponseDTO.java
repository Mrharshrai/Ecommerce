package com.shop.productservice.DTOs.ProductActivityDTOs;

import com.shop.productservice.enums.ProductActivityType;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductActivityStatsResponseDTO {

    // Activity Count By Type
    private Map<ProductActivityType, Long> activityCountByType;

    // Activities Per Admin
    private Map<String, Long> activityCountByAdmin;

    // Activities Per Day
    private Map<LocalDate, Long> dailyActivityCount;
}
