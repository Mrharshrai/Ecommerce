package com.shop.productservice.DTOs.VariantDTOs.RequestDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductVariantRequestDTO {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @Size(max = 255, message = "Variant name must be less than 255 characters")
    private String variantName;
}
