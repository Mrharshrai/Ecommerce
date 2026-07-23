package com.shop.productservice.DTOs.ImageDTOs.RequestDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductVariantImageRequestDTO {

    @NotNull(message = "Image ID is required")
    private Long imageId;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String image;

    @Min(value = 1, message = "Sort order must be greater than or equal to 1")
    private Integer sortOrder;

    @Size(max = 255, message = "Alt text must be less than 255 characters")
    private String altText;
}
