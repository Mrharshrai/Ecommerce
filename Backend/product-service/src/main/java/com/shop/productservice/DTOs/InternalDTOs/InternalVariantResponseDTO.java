package com.shop.productservice.DTOs.InternalDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalVariantResponseDTO {

    private String productName;
    private String color;
    private BigDecimal price;         // MRP (original price)
    private BigDecimal sellingPrice;  // Price after discount (equals MRP if no discount)
    private BigDecimal discountAmount; // Absolute discount value (MRP - sellingPrice)
    private Integer discountPercent;   // Discount % (null if FLAT or no discount)
    private boolean hasDiscount;       // true if any active discount applies
    private String imageUrl;
    private List<InternalSizeInfo> sizes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InternalSizeInfo {
        private String size;
        private Integer quantity;
    }
}