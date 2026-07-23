package com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs;

import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantResponseDTO;
import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String asin;
    private String name;
    private String description;

    private String category;
    private String subCategory;

    private String brand;
    private String material;

    private Gender gender;
    private AgeGroup ageGroup;

    private List<String> tags;
    private List<String> highlights;

    private Integer totalProductQuantity;

    private boolean isActive;

    private boolean isDeleted;

    private boolean isPublished;

    private List<ProductVariantResponseDTO> variants;
}
