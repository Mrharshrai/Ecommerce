package com.shop.productservice.DTOs.ProductActivityDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductActivitySummaryResponseDTO {

    // Overall
    private Long totalActivities;

    // Product
    private Long totalProductActivities;

    // Variant
    private Long totalVariantActivities;

    // Size
    private Long totalSizeActivities;

    // Image
    private Long totalImageActivities;

    // Related Product
    private Long totalRelatedProductActivities;

    // Discount
    private Long totalDiscountActivities;

    // User/Admin
    private Long totalActiveAdmins;

    // Time Based
    private Long todayActivities;

    private Long thisWeekActivities;

    private Long thisMonthActivities;
}
