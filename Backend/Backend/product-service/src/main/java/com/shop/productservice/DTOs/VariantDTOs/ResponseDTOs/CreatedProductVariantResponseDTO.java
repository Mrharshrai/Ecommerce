package com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedProductVariantResponseDTO {

    private Long variantId;

    private Long productId;

    private String skuCode;

    private String variantName;

    private String message;
}

