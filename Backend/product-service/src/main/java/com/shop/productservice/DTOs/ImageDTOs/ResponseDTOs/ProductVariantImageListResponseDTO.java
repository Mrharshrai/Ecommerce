package com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantImageListResponseDTO {

    private Long imageId;
    private String image;
    private String altText;
    private Integer sortOrder;
    private boolean isActive;
    private boolean isDeleted;
}

