package com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantImageResponseDTO {

    private Long imageId;

    private Long variantId;
    private String variantName ;

    private Long productId;
    private String productName ;

    private String image;

    private Integer sortOrder;

    private String altText;

    private boolean isActive;

    private boolean isDeleted;
}

