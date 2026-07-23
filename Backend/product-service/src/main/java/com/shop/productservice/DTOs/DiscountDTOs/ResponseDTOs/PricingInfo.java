package com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingInfo {

    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private boolean hasDiscount;
    private String discountName;
    private String discountCode;
}
