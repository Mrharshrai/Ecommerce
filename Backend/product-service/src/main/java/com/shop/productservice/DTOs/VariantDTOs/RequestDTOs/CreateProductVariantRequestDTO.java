package com.shop.productservice.DTOs.VariantDTOs.RequestDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductVariantRequestDTO {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Variant name cannot be empty")
    @Size(max = 255, message = "Variant name must be less than 255 characters")
    private String variantName;

    @NotBlank(message = "Color cannot be empty")
    @Size(max = 100, message = "Color must be less than 100 characters")
    private String color;
}
