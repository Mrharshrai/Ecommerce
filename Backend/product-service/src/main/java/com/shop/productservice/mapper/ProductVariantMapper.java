package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.CreateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.CreatedProductVariantResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantListResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantResponseDTO;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.entity.ProductVariantSize;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.PricingInfo;
import com.shop.productservice.service.other.DiscountService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductVariantMapper {

    private final ProductVariantSizeMapper sizeMapper;
    private final ProductVariantImageMapper imageMapper;
    private final DiscountService discountService;

    public ProductVariantMapper(ProductVariantSizeMapper sizeMapper,
                                ProductVariantImageMapper imageMapper,
                                DiscountService discountService) {
        this.sizeMapper = sizeMapper;
        this.imageMapper = imageMapper;
        this.discountService = discountService;
    }

    // ---------------- CREATE: DTO -> ENTITY ----------------
    public ProductVariant toEntity(CreateProductVariantRequestDTO dto) {

        if (dto == null) return null;

        return ProductVariant.builder()
                .id(null)
                .skuCode(null) // service sets
                .variantName(dto.getVariantName())
                .color(dto.getColor().toUpperCase().trim())
                .sizes(Collections.emptyList())   // added later in service
                .images(Collections.emptyList())  // added later in service
//                .totalProductVariantQuantity(0)
                .product(null)   // set in service
//                .isActive(true)
//                .isDeleted(false)
                .build();
    }

    // ---------------- AFTER CREATE RESPONSE ----------------
    public CreatedProductVariantResponseDTO toCreatedResponseDTO(ProductVariant variant) {
        if (variant == null) return null;

        return CreatedProductVariantResponseDTO.builder()
                .variantId(variant.getId())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .skuCode(variant.getSkuCode())
                .variantName(variant.getVariantName())
                .message("Variant created successfully.")
                .build();
    }

    // ---------------- FULL RESPONSE (DETAILS) ----------------
    public ProductVariantResponseDTO toResponseDTO(ProductVariant variant) {
        if (variant == null) return null;

        return ProductVariantResponseDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .skuCode(variant.getSkuCode())
                .variantName(variant.getVariantName())
                .color(variant.getColor())
                .totalProductVariantQuantity(variant.getTotalProductVariantQuantity())
                .isActive(variant.isActive())
                .isDeleted(variant.isDeleted())

                // Nested mapping using other mappers
                .sizes(sizeMapper.toResponseDTOs(variant.getSizes()))
                .images(imageMapper.toResponseDTOs(variant.getImages()))

                .build();
    }

    public List<ProductVariantResponseDTO> toResponseDTOs(List<ProductVariant> variants) {
        if (variants == null) return Collections.emptyList();
        return variants.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // ---------------- LIST MAPPER (LIST VIEW) ----------------
    public ProductVariantListResponseDTO toListDTO(ProductVariant variant) {
        if (variant == null) return null;

        BigDecimal minMrp = calculateStartingPrice(variant);
        PricingInfo pricingInfo = null;
        if (variant.getProduct() != null) {
            pricingInfo = discountService.calculatePricing(variant.getProduct(), minMrp);
        }

        return ProductVariantListResponseDTO.builder()
                .id(variant.getId())
                .skuCode(variant.getSkuCode())
                .variantName(variant.getVariantName())
                .color(variant.getColor())
                .isActive(variant.isActive())
                .isDeleted(variant.isDeleted())
                .startingPrice(minMrp)
                .sellingPrice(pricingInfo != null ? pricingInfo.getSellingPrice() : minMrp)
                .discountAmount(pricingInfo != null ? pricingInfo.getDiscountAmount() : BigDecimal.ZERO)
                .discountPercent(pricingInfo != null ? pricingInfo.getDiscountPercent() : 0)
                .hasDiscount(pricingInfo != null && pricingInfo.isHasDiscount())
                .primaryImageUrl(findPrimaryImage(variant))
                .build();
    }

    public List<ProductVariantListResponseDTO> toListDTOs(List<ProductVariant> variants) {
        if (variants == null) return Collections.emptyList();
        return variants.stream().map(this::toListDTO).collect(Collectors.toList());
    }

    // ---------------- SUPPORT METHODS ----------------
    public BigDecimal calculateStartingPrice(ProductVariant variant) {
        if (variant == null || variant.getSizes() == null || variant.getSizes().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return variant.getSizes().stream()
                .map(ProductVariantSize::getMrp)
                .filter(mrp -> mrp != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public String findPrimaryImage(ProductVariant variant) {
        if (variant == null || variant.getImages() == null || variant.getImages().isEmpty()) {
            return null;
        }

        return variant.getImages().stream()
                .filter(img -> img.getSortOrder() != null && img.getSortOrder() == 1)
                .map(ProductVariantImage::getImage)
                .findFirst()
                .orElse(variant.getImages().get(0).getImage());
    }
}






















































//@Mapper(
//        componentModel = "spring",
//        uses = {
//                ProductVariantSizeMapper.class,
//                ProductVariantImageMapper.class
//        }
//)
//public interface ProductVariantMapper {
//
//    // ---------------- CREATE: DTO -> ENTITY ----------------
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "skuCode", ignore = true)  // service generates
//    @Mapping(target = "sizes", ignore = true)    // service adds sizes later
//    @Mapping(target = "images", ignore = true) // service adds images later
//    @Mapping(target = "totalProductVariantQuantity", ignore = true)
//    @Mapping(target = "product", ignore = true)   // service sets product
//    @Mapping(target = "isActive", constant = "true")
//    @Mapping(target = "isDeleted", constant = "false")
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    ProductVariant toEntity(CreateProductVariantRequestDTO dto);
//
//    // ---------------- AFTER CREATE RESPONSE ----------------
//
//    @Mapping(source = "id", target = "variantId")
//    @Mapping(source = "product.id", target = "productId")
//    @Mapping(source = "skuCode", target = "skuCode")
//    @Mapping(source = "variantName", target = "variantName")
//    @Mapping(target = "message", constant = "Variant created successfully.")
//    CreatedProductVariantResponseDTO toCreatedResponseDTO(ProductVariant variant);
//
//    // ---------------- FULL RESPONSE FOR PRODUCT DETAILS ----------------
//    @Named("toVariantResponseDTO")
//    @Mapping(source = "id", target = "id")
//    @Mapping(source = "skuCode", target = "skuCode")
//    @Mapping(source = "variantName", target = "variantName")
//    @Mapping(source = "color", target = "color")
//    @Mapping(source = "totalProductVariantQuantity", target = "totalProductVariantQuantity")
//    @Mapping(source = "isActive", target = "isActive")
//
//    // Nested mapping handled by other mappers
//    @Mapping(source = "sizes", target = "sizes", qualifiedByName = "toSizeResponseDTOs")
//    // ✅ handled by ProductVariantSizeMapper
//    @Mapping(source = "images", target = "images", qualifiedByName = "toImageResponseDTOs")
//    // ✅ handled by ProductVariantImageMapper
//
//    ProductVariantResponseDTO toResponseDTO(ProductVariant variant);
//
//    @Named("toVariantResponseDTOs")
//    List<ProductVariantResponseDTO> toResponseDTOs(List<ProductVariant> variants);
//
//
//    @Mapping(source = "id", target = "id")
//    @Mapping(source = "skuCode", target = "skuCode")
//    @Mapping(source = "variantName", target = "variantName")
//    @Mapping(source = "color", target = "color")
//    @Mapping(source = "isActive", target = "isActive")
//    // starting price = minimum MRP across all sizes
//    @Mapping(target = "startingPrice", expression = "java(calculateStartingPrice(variant))")
//    // primary image = first image of first variant
//    @Mapping(target = "primaryImageUrl", expression = "java(findPrimaryImage(variant))")
//    ProductVariantListResponseDTO toListDTO(ProductVariant variant);
//
//    List<ProductVariantListResponseDTO> toListDTOs(List<ProductVariant> variants);
//
//    // ---------------- SUPPORT METHODS FOR EXPRESSIONS ----------------
//
//    default BigDecimal calculateStartingPrice(ProductVariant variant) {
//        if (variant == null || variant.getSizes() == null || variant.getSizes().isEmpty()) {
//            return BigDecimal.ZERO;
//        }
//
//        return variant.getSizes().stream()
//                .filter(size -> size.getMrp() != null)
//                .map(ProductVariantSize::getMrp)
//                .min(BigDecimal::compareTo)
//                .orElse(BigDecimal.ZERO);
//    }
//
//    default String findPrimaryImage(ProductVariant variant) {
//        if (variant == null || variant.getImages() == null || variant.getImages().isEmpty()) {
//            return null;
//        }
//
//        // 1. Try to find sortOrder = 1
//        return variant.getImages().stream()
//                .filter(img -> img.getSortOrder() != null && img.getSortOrder() == 1)
//                .map(ProductVariantImage::getImage)
//                .findFirst()
//                // 2. Else take first image
//                .orElse(variant.getImages().get(0).getImage());
//    }
//}
