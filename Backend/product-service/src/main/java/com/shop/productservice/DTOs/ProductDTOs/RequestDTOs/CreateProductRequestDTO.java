package com.shop.productservice.DTOs.ProductDTOs.RequestDTOs;

import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequestDTO {

    @NotBlank(message = "ASIN is required")
    @Size(max = 100, message = "ASIN can be at most 100 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "ASIN can contain only letters, numbers, hyphens (-), and underscores (_). Spaces and special characters are not allowed."
    )
    private String asin;

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    @NotBlank(message = "Category cannot be empty")
    @Size(max = 100, message = "Category must be under 100 characters")
    private String category;

    @Size(max = 100, message = "subCategory must be under 100 characters")
    private String subCategory;

    @NotBlank(message = "Brand cannot be empty")
    @Size(max = 100, message = "brand must be under 100 characters")
    private String brand;

    @NotBlank(message = "Material is required")
    @Size(max = 255, message = "material must be under 255 characters")
    private String material;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "AgeGroup is required")
    private AgeGroup ageGroup;

    private List<@Size(max = 50, message = "tags per column must be under 50 characters") String> tags;

    private List<@Size(max = 300, message = "highlights per column must be under 300 characters") String> highlights;
}
