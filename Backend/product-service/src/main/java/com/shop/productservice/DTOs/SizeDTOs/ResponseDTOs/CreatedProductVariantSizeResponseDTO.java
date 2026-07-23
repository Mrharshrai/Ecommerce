package com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedProductVariantSizeResponseDTO {

    private Long sizeId;

    private Long variantId;

    private Long productId;

    private String sizeSku;

    private String message;
}
