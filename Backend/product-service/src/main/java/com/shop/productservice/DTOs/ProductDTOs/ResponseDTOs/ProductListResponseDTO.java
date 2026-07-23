package com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// for home page to show minimum data for products, trending products or offers
public class ProductListResponseDTO {

    private Long id;
    private String asin;
    private String name;
    private String brand;
    private String category;

    // minimum price (smallest MRP across all variant sizes)
    private BigDecimal startingPrice;

    // primary image (sortOrder = 1)
    private String primaryImageUrl;

    private boolean isActive;

    private boolean isDeleted;

    private boolean isPublished;

    // Discount details
    private BigDecimal sellingPrice;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private boolean hasDiscount;
}

