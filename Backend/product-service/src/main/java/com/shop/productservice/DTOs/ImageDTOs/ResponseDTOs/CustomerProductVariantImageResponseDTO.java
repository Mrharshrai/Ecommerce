package com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProductVariantImageResponseDTO {

    private Long imageId;

    private String image;

    private Integer sortOrder;

    private String altText;
}
