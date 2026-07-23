package com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductVariantImageResponseDTO {

    private Long imageId;
    private Long variantId;
    private Long productId;
    private String image;
    private List<String> updatedFields;
    private String message;
}
