package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.CustomerProductVariantResponseDTO;
import com.shop.productservice.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerProductVariantMapper {

    private final CustomerProductVariantSizeMapper sizeMapper;
    private final CustomerProductVariantImageMapper imageMapper;

    public CustomerProductVariantMapper(
            CustomerProductVariantSizeMapper sizeMapper,
            CustomerProductVariantImageMapper imageMapper) {

        this.sizeMapper = sizeMapper;
        this.imageMapper = imageMapper;
    }

    // -------------------------------------------------------------
    // ENTITY → CUSTOMER RESPONSE DTO
    // -------------------------------------------------------------
    public CustomerProductVariantResponseDTO toResponseDTO(ProductVariant variant) {

        if (variant == null) {
            return null;
        }

        return CustomerProductVariantResponseDTO.builder()
                .id(variant.getId())
                .skuCode(variant.getSkuCode())
                .variantName(variant.getVariantName())
                .color(variant.getColor())

                // Nested mapping
                .sizes(sizeMapper.toResponseDTOs(variant.getSizes()))
                .images(imageMapper.toResponseDTOs(variant.getImages()))

                .build();
    }

    // -------------------------------------------------------------
    // ENTITY LIST → CUSTOMER RESPONSE DTO LIST
    // -------------------------------------------------------------
    public List<CustomerProductVariantResponseDTO> toResponseDTOs(List<ProductVariant> variants) {

        List<CustomerProductVariantResponseDTO> list = new ArrayList<>();

        if (variants == null || variants.isEmpty()) {
            return list;
        }

        for (ProductVariant variant : variants) {
            list.add(toResponseDTO(variant));
        }

        return list;
    }

}
