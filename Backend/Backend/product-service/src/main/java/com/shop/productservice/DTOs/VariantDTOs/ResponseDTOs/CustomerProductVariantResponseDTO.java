package com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.CustomerProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.CustomerProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProductVariantResponseDTO {
    private Long id;
//    private Long productId;
//    private String productName ;
    private String skuCode;
    private String variantName;
    private String color;

    private List<CustomerProductVariantSizeResponseDTO> sizes;
    private List<CustomerProductVariantImageResponseDTO> images;

}
