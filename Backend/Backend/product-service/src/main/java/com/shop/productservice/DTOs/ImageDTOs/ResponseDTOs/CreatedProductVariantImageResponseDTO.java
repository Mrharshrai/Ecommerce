package com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedProductVariantImageResponseDTO {

    private Long imageId;

    private Long variantId;

    private Long productId;

    private String image;

    private String message;
}

