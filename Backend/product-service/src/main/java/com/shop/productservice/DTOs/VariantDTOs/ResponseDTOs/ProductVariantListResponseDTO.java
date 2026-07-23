package com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantListResponseDTO {

    private Long id;

    private String skuCode;

    private String variantName;

    private String color;

    // Primary display image (sortOrder = 1)
    private String primaryImageUrl;

    // Minimum MRP among all sizes
    private BigDecimal startingPrice;

    private boolean isActive;

    private boolean isDeleted;

    private BigDecimal sellingPrice;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private boolean hasDiscount;
}


