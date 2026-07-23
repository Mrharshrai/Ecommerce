package com.shop.productservice.DTOs.ProductDTOs.RequestDTOs;

import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequestDTO {

    // ✅ Product ID required to know which product to update
    @NotNull(message = "Product ID is required")
    private Long productId;

    // ✅ ASIN CANNOT BE UPDATED (business rule)
    // So we do NOT include asin here

    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @Size(max = 100, message = "Sub-category must be less than 100 characters")
    private String subCategory;

    @Size(max = 100, message = "Brand must be less than 100 characters")
    private String brand;

    @Size(max = 255, message = "Material must be less than 255 characters")
    private String material;

    private Gender gender;

    private AgeGroup ageGroup;

    // ✅ Full replace list
    @Valid
    private List<@Size(max = 50) String> tags;

    // ✅ Full replace list
    @Valid
    private List<@Size(max = 300) String> highlights;

}
