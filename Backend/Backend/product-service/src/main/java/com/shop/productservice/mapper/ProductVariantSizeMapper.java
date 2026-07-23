package com.shop.productservice.mapper;


import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.CreateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.CreatedProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeListResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;
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
public class ProductVariantSizeMapper {

    private final DiscountService discountService;

    public ProductVariantSizeMapper(DiscountService discountService) {
        this.discountService = discountService;
    }

    // ---------------- CREATE: DTO -> ENTITY ----------------
    public ProductVariantSize toEntity(CreateProductVariantSizeRequestDTO dto) {

        if (dto == null) return null;

        return ProductVariantSize.builder()
                .id(null)
                .sizeSku(null)          // generated in SERVICE
                .size(dto.getSize())
                .quantity(dto.getQuantity())
                .mrp(dto.getMrp())
                .weight(dto.getWeight())
                .length(dto.getLength())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .variant(null)          // set in SERVICE
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    // ---------------- AFTER CREATE RESPONSE ----------------
    public CreatedProductVariantSizeResponseDTO toCreatedResponseDTO(ProductVariantSize size) {
        if (size == null) return null;

        return CreatedProductVariantSizeResponseDTO.builder()
                .sizeId(size.getId())
                .variantId(size.getVariant() != null ? size.getVariant().getId() : null)
                .productId(
                        size.getVariant() != null && size.getVariant().getProduct() != null
                                ? size.getVariant().getProduct().getId()
                                : null
                )
                .sizeSku(size.getSizeSku())
                .message("Size created successfully.")
                .build();
    }

    // ---------------- FULL RESPONSE (DETAIL VIEW) ----------------
    public ProductVariantSizeResponseDTO toResponseDTO(ProductVariantSize size) {
        if (size == null) return null;

        PricingInfo pricingInfo = null;
        if (size.getVariant() != null && size.getVariant().getProduct() != null && size.getMrp() != null) {
            pricingInfo = discountService.calculatePricing(size.getVariant().getProduct(), size.getMrp());
        }

        return ProductVariantSizeResponseDTO.builder()
                .sizeId(size.getId())
                .sizeSku(size.getSizeSku())
                .variantId(size.getVariant().getId())
                .variantName(size.getVariant().getVariantName())
                .productId(size.getVariant().getProduct().getId())
                .productName(size.getVariant().getProduct().getName())
                .size(size.getSize())
                .quantity(size.getQuantity())
                .mrp(size.getMrp())
                .weight(size.getWeight())
                .length(size.getLength())
                .width(size.getWidth())
                .height(size.getHeight())
                .isActive(size.isActive())
                .isDeleted(size.isDeleted())
                .sellingPrice(pricingInfo != null ? pricingInfo.getSellingPrice() : size.getMrp())
                .discountAmount(pricingInfo != null ? pricingInfo.getDiscountAmount() : BigDecimal.ZERO)
                .discountPercent(pricingInfo != null ? pricingInfo.getDiscountPercent() : 0)
                .hasDiscount(pricingInfo != null && pricingInfo.isHasDiscount())
                .discountName(pricingInfo != null ? pricingInfo.getDiscountName() : null)
                .discountCode(pricingInfo != null ? pricingInfo.getDiscountCode() : null)
                .build();
    }

    public List<ProductVariantSizeResponseDTO> toResponseDTOs(List<ProductVariantSize> sizes) {
        if (sizes == null) return Collections.emptyList();
        return sizes.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // ---------------- LIST VIEW ----------------
    public ProductVariantSizeListResponseDTO toListDTO(ProductVariantSize size) {
        if (size == null) return null;

        return ProductVariantSizeListResponseDTO.builder()
                .sizeId(size.getId())
                .sizeSku(size.getSizeSku())
                .size(size.getSize())
                .quantity(size.getQuantity())
                .mrp(size.getMrp())
                .isActive(size.isActive())
                .isDeleted(size.isDeleted())
                .build();
    }

    public List<ProductVariantSizeListResponseDTO> toListDTOs(List<ProductVariantSize> sizes) {
        if (sizes == null) return Collections.emptyList();
        return sizes.stream().map(this::toListDTO).collect(Collectors.toList());
    }
}