package com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs;

import com.shop.productservice.enums.Size;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProductVariantSizeResponseDTO {

    private Long sizeId;

    private String sizeSku;

    private Size size;

    private Integer quantity;

    private BigDecimal mrp;

    // Pricing
    private BigDecimal sellingPrice;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private boolean hasDiscount;
    private String discountName;
    private String discountCode;
}
