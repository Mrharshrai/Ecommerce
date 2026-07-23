package com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponseDTO {

    private Long id;
    private Long productId;
    private String productName ;
    private String skuCode;
    private String variantName;
    private String color;

    private Integer totalProductVariantQuantity;

    private boolean isActive;

    private boolean isDeleted;

    private List<ProductVariantSizeResponseDTO> sizes;
    private List<ProductVariantImageResponseDTO> images;
}

