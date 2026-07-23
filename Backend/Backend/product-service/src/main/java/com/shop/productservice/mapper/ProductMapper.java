package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.CreateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.CreatedProductResponseDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.ProductListResponseDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.ProductResponseDTO;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.entity.ProductVariantSize;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.PricingInfo;
import com.shop.productservice.service.other.DiscountService;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProductMapper {

    private final ProductVariantMapper productVariantMapper;
    private final DiscountService discountService;

    public ProductMapper(ProductVariantMapper productVariantMapper, DiscountService discountService) {
        this.productVariantMapper = productVariantMapper;
        this.discountService = discountService;
    }

    //DTO → ENTITY   (CREATE PRODUCT)
    public Product toEntity(CreateProductRequestDTO dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setId(null);                             // ignore id
        product.setVariants(new ArrayList<>());          // ignore variants
        product.setTotalProductQuantity(null);           // ignore qty
        product.setActive(true);                         // constant true
        product.setDeleted(false);                       // constant false
        product.setPublished(false);                     // ignore
        product.setPublishedAt(null);
        product.setCreatedAt(null);
        product.setUpdatedAt(null);

        // copy simple fields
        product.setAsin(dto.getAsin().trim().toUpperCase());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory().trim().toUpperCase());
        product.setSubCategory(dto.getSubCategory());
        product.setBrand(dto.getBrand());
        product.setMaterial(dto.getMaterial());
        product.setGender(dto.getGender());
        product.setAgeGroup(dto.getAgeGroup());
        product.setTags(dto.getTags());
        product.setHighlights(dto.getHighlights());

        return product;
    }

    //ENTITY → CreatedProductResponseDTO
    public CreatedProductResponseDTO toCreatedResponseDTO(Product product) {
        if (product == null) return null;

        return CreatedProductResponseDTO.builder()
                .id(product.getId())
                .asin(product.getAsin())
                .name(product.getName())
                .message("Product created successfully.")
                .build();
    }

    //ENTITY → LIST RESPONSE DTO
    public ProductListResponseDTO toListDTO(Product product) {
        if (product == null) return null;

        BigDecimal minMrp = calculateStartingPrice(product);
        PricingInfo pricingInfo = discountService.calculatePricing(product, minMrp);

        return ProductListResponseDTO.builder()
                .id(product.getId())
                .asin(product.getAsin())
                .name(product.getName())
                .brand(product.getBrand())
                .category(product.getCategory())
                .isActive(product.isActive())
                .isDeleted(product.isDeleted())
                .isPublished(product.isPublished())
                .startingPrice(minMrp)
                .sellingPrice(pricingInfo.getSellingPrice())
                .discountAmount(pricingInfo.getDiscountAmount())
                .discountPercent(pricingInfo.getDiscountPercent())
                .hasDiscount(pricingInfo.isHasDiscount())
                .primaryImageUrl(findPrimaryImage(product))
                .build();
    }

    public List<ProductListResponseDTO> toListDTOs(List<Product> products) {
        if (products == null) return null;

        List<ProductListResponseDTO> list = new ArrayList<>();
        for (Product p : products) {
            list.add(toListDTO(p));
        }
        return list;
    }

    //                ENTITY → FULL RESPONSE DTO
    // -------------------------------------------------------------
    public ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) return null;

        return ProductResponseDTO.builder()
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
                .totalProductQuantity(product.getTotalProductQuantity())
                .isActive(product.isActive())
                .isDeleted(product.isDeleted())
                .isPublished(product.isPublished())
                .variants(productVariantMapper.toResponseDTOs(product.getVariants()))
                .build();
    }

    //UTILITY METHODS  (same logic as MapStruct defaults)
    //* Calculates the lowest MRP from:
    // * - ACTIVE variants
    // * - ACTIVE & NON-DELETED sizes
    // * - quantity > 0 only
    // * If no valid size found → returns BigDecimal.ZERO
    public BigDecimal calculateStartingPrice(Product product) {
        if (product == null ||
                product.getVariants() == null ||
                product.getVariants().isEmpty())
            return BigDecimal.ZERO;

        return product.getVariants().stream()
                // ✅ only active & non-deleted variants
                .filter(v -> v.isActive() && !v.isDeleted())
                .filter(v -> v.getSizes() != null)

                .flatMap(v -> v.getSizes().stream())

                // ✅ only active & non-deleted sizes
                .filter(s -> s.isActive() && !s.isDeleted())

                // ✅ only in-stock sizes
                .filter(s -> s.getQuantity() != null && s.getQuantity() > 0)

                // ✅ valid MRP
                .map(ProductVariantSize::getMrp)
                .filter(Objects::nonNull)

                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Finds the primary image for a product listing.
     *
     * Business rules:
     * 1️⃣ Consider ONLY ACTIVE & NON-DELETED variants
     * 2️⃣ For each variant (in order):
     *      - Pick the ACTIVE & NON-DELETED image with the LOWEST sortOrder
     *      - If sortOrder=1 image is inactive, try sortOrder=2, then 3, ...
     * 3️⃣ If a variant has NO valid images, move to the NEXT variant
     * 4️⃣ DO NOT mix images across variants
     * 5️⃣ First variant with a valid image wins
     *
     * @param product Product entity
     * @return image URL or null if none found
     */
    public String findPrimaryImage(Product product) {
        if (product == null || product.getVariants() == null) return null;

        return product.getVariants().stream()

                // ✅ only active & non-deleted variants
                .filter(variant -> variant.isActive() && !variant.isDeleted())
                .filter(variant -> variant.getImages() != null && !variant.getImages().isEmpty())

                // 🔁 for each variant, find its best image FIRST
                .map(variant ->
                        variant.getImages().stream()

                                // ✅ only active & non-deleted images
                                .filter(image -> image.isActive() && !image.isDeleted())

                                // ✅ lowest sortOrder wins (1 preferred)
                                .sorted(Comparator.comparing(
                                        ProductVariantImage::getSortOrder,
                                        Comparator.nullsLast(Integer::compareTo)
                                ))

                                // 🔑 best image of THIS variant
                                .map(ProductVariantImage::getImage)
                                .findFirst()
                                .orElse(null)
                )

                // ❌ remove variants that had no valid images
                .filter(Objects::nonNull)

                // ✅ first variant with a valid image wins
                .findFirst()
                .orElse(null);
    }

}







