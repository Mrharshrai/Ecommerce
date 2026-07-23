package com.shop.productservice.DTOs.RelatedProductDTOs.RequestDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRelatedProductRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Related product variant ID is required")
    private Long relatedProductVariantId;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    @Max(value = 5, message = "Display order cannot be greater than 5")
    private Integer displayOrder;
}
