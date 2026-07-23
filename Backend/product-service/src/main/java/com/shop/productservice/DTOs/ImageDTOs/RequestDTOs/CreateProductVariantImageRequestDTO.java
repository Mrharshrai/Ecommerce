package com.shop.productservice.DTOs.ImageDTOs.RequestDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductVariantImageRequestDTO {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String image;

    @NotNull(message = "Sort order is required")
    @Min(value = 1, message = "Sort order must be greater than or equal to 1")
    private Integer sortOrder;

    @Size(max = 255, message = "Alt text must be less than 255 characters")
    private String altText;
}