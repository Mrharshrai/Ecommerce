package com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductVariantResponseDTO {
    private Long variantId;

    private Long productId;

    private String skuCode;

    private String variantName;

    // list of fields that were updated (e.g. ["variantName", "color"])
    private List<String> updatedFields;

    private String message;
}

