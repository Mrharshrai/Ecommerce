package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.CustomerProductVariantImageResponseDTO;
import com.shop.productservice.entity.ProductVariantImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerProductVariantImageMapper {

    // -------------------------------------------------------------
    // ENTITY → CUSTOMER RESPONSE DTO
    // -------------------------------------------------------------
    public CustomerProductVariantImageResponseDTO toResponseDTO(ProductVariantImage image) {

        if (image == null) {
            return null;
        }

        return CustomerProductVariantImageResponseDTO.builder()
                .imageId(image.getId())
                .image(image.getImage())
                .sortOrder(image.getSortOrder())
                .altText(image.getAltText())
                .build();
    }

    // -------------------------------------------------------------
    // ENTITY LIST → CUSTOMER RESPONSE DTO LIST
    // -------------------------------------------------------------
    public List<CustomerProductVariantImageResponseDTO> toResponseDTOs(List<ProductVariantImage> images) {

        List<CustomerProductVariantImageResponseDTO> list = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            return list;
        }

        for (ProductVariantImage image : images) {
            list.add(toResponseDTO(image));
        }
        return list;
    }
}
