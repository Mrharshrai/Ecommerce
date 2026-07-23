package com.shop.productservice.DTOs.BulkImportDTOs.RequestDTOs;

import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportRow {

    @NotBlank(message = "product_name is required")
    @Size(max = 255, message = "product_name must be under 255 characters")
    private String productName;

    @NotBlank(message = "product_description is required")
    @Size(max = 500, message = "product_description must be under 500 characters")
    private String productDescription;

    @NotBlank(message = "product_category is required")
    @Size(max = 100, message = "product_category must be under 100 characters")
    private String productCategory;

    @Size(max = 100, message = "product_subCategory must be under 100 characters")
    private String productSubCategory;

    @NotBlank(message = "product_brand is required")
    @Size(max = 100, message = "product_brand must be under 100 characters")
    private String productBrand;

    @NotBlank(message = "product_material is required")
    @Size(max = 255, message = "product_material must be under 255 characters")
    private String productMaterial;

    @NotNull(message = "product_gender is required")
    private Gender productGender;

    @NotNull(message = "product_ageGroup is required")
    private AgeGroup productAgeGroup;

    @NotBlank(message = "product_asin is required")
    @Size(max = 100, message = "product_asin can be at most 100 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "product_asin can contain only letters, numbers, hyphens (-), and underscores (_)"
    )
    private String productAsin;

    @NotBlank(message = "variant_name is required")
    @Size(max = 255, message = "variant_name must be under 255 characters")
    private String variantName;

    @NotBlank(message = "variant_color is required")
    @Size(max = 100, message = "variant_color must be under 100 characters")
    private String variantColor;

    @NotNull(message = "size is required")
    private com.shop.productservice.enums.Size size;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "mrp is required")
    @DecimalMin(value = "0.01", message = "mrp must be greater than 0")
    private BigDecimal mrp;

    @NotNull(message = "weight is required")
    @DecimalMin(value = "0.01", message = "weight must be greater than 0")
    private BigDecimal weight;

    @NotNull(message = "length is required")
    @DecimalMin(value = "0.01", message = "length must be greater than 0")
    private BigDecimal length;

    @NotNull(message = "width is required")
    @DecimalMin(value = "0.01", message = "width must be greater than 0")
    private BigDecimal width;

    @NotNull(message = "height is required")
    @DecimalMin(value = "0.01", message = "height must be greater than 0")
    private BigDecimal height;

    @Builder.Default
    private List<String> tags = new java.util.ArrayList<>();

    @Builder.Default
    private List<String> highlights = new java.util.ArrayList<>();
}
