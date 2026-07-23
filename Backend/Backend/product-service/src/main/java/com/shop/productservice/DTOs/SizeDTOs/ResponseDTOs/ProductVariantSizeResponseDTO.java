package com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs;

import com.shop.productservice.enums.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantSizeResponseDTO {

    private Long sizeId;
    private String sizeSku; // auto-generated: skuCode-size

    private Long variantId;
    private String variantName ;

    private Long productId;
    private String productName ;

    private Size size;

    private Integer quantity;

    private BigDecimal mrp;

    private BigDecimal weight;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    private boolean isActive;

    private boolean isDeleted;

    private BigDecimal sellingPrice;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private boolean hasDiscount;
    private String discountName;
    private String discountCode;
}

