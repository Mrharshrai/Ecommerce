package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.CustomerProductResponseDTO;
import com.shop.productservice.entity.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerProductMapper {

    private final CustomerProductVariantMapper customerProductVariantMapper;

    public CustomerProductMapper(CustomerProductVariantMapper customerProductVariantMapper) {
        this.customerProductVariantMapper = customerProductVariantMapper;
    }

    // -------------------------------------------------------------
    // ENTITY → CUSTOMER FULL RESPONSE DTO
    // -------------------------------------------------------------
    public CustomerProductResponseDTO toResponseDTO(Product product) {

        if (product == null) {
            return null;
        }

        return CustomerProductResponseDTO.builder()
                .id(product.getId())
                .asin(product.getAsin())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .subCategory(product.getSubCategory())
                .brand(product.getBrand())
                .material(product.getMaterial())
                .gender(product.getGender())
                .ageGroup(product.getAgeGroup())
                .tags(product.getTags())
                .highlights(product.getHighlights())

                // Nested mapping
                .variants(customerProductVariantMapper.toResponseDTOs(product.getVariants()))

                .build();
    }

    // -------------------------------------------------------------
    // ENTITY LIST → CUSTOMER RESPONSE DTO LIST
    // -------------------------------------------------------------
    public List<CustomerProductResponseDTO> toResponseDTOs(List<Product> products) {

        List<CustomerProductResponseDTO> list = new ArrayList<>();

        if (products == null || products.isEmpty()) {
            return list;
        }

        for (Product product : products) {
            list.add(toResponseDTO(product));
        }

        return list;
    }

}
