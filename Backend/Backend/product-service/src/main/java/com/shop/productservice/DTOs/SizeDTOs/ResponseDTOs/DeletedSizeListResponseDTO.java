package com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs;

import com.shop.productservice.enums.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedSizeListResponseDTO {

    private Long sizeId;

    private Long variantId;

    private Long productId;

    private String sizeSku;

    private Size size;

    private boolean isActive;

    private boolean isDeleted;
}
