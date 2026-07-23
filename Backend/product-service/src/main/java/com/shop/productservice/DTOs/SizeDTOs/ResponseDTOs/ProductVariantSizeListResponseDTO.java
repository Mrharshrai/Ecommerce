package com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs;

import com.shop.productservice.enums.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantSizeListResponseDTO {

    private Long sizeId;
    private String sizeSku;
    private Size size;
    private Integer quantity;
    private BigDecimal mrp;
    private boolean isActive;
    private boolean isDeleted;
}

