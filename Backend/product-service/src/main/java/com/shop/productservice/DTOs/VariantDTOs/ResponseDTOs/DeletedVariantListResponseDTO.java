package com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedVariantListResponseDTO {

    private Long variantId;

    private Long productId;

    private String skuCode;

    private String variantName;

    private String color;

    private boolean isActive;

    private boolean isDeleted;
}

