package com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs;

import com.shop.productservice.enums.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductVariantSizeResponseDTO {

    private Long sizeId;
    private Long variantId;
    private Long productId;
    private Size size;
    private List<String> updatedFields;
    private String message;
}
