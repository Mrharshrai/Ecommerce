package com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedImageListResponseDTO {

    private Long imageId;

    private Long variantId;

    private Long productId;

    private String image;

    private boolean isActive;

    private boolean isDeleted;

}
