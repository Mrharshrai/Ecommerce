package com.shop.productservice.mapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.PricingInfo;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.CustomerProductVariantSizeResponseDTO;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.service.other.DiscountService;
import org.springframework.stereotype.Component;

@Component
public class CustomerProductVariantSizeMapper {

    private final DiscountService discountService;

    public CustomerProductVariantSizeMapper(DiscountService discountService) {
        this.discountService = discountService;
    }

    // -------------------------------------------------------------
    // ENTITY → CUSTOMER RESPONSE DTO
    // -------------------------------------------------------------
    public CustomerProductVariantSizeResponseDTO toResponseDTO(ProductVariantSize size) {

        if (size == null) {
            return null;
        }

        // ---------------------------------------------------------
        // Calculate customer-facing pricing.
        //
        // If the product has an active applicable discount,
        // calculate:
        // • Selling Price
        // • Discount Amount
        // • Discount Percentage
        // • Discount Name
        // • Discount Code
        //
        // Otherwise, customer pays the MRP.
        // ---------------------------------------------------------

        PricingInfo pricingInfo = null;
        if (size.getVariant() != null && size.getVariant().getProduct() != null && size.getMrp() != null) {
            pricingInfo = discountService.calculatePricing(size.getVariant().getProduct(), size.getMrp());
        }

        return CustomerProductVariantSizeResponseDTO.builder()
                .sizeId(size.getId())
                .sizeSku(size.getSizeSku())
                .size(size.getSize())
                .quantity(size.getQuantity())
                .mrp(size.getMrp())
                .sellingPrice(pricingInfo != null ? pricingInfo.getSellingPrice() : size.getMrp())
                .discountAmount(pricingInfo != null ? pricingInfo.getDiscountAmount() : BigDecimal.ZERO)
                .discountPercent(pricingInfo != null ? pricingInfo.getDiscountPercent() : 0)
                .hasDiscount(pricingInfo != null && pricingInfo.isHasDiscount())
                .discountName(pricingInfo != null ? pricingInfo.getDiscountName() : null)
                .discountCode(pricingInfo != null ? pricingInfo.getDiscountCode() : null)
                .build();
    }

    // -------------------------------------------------------------
    // ENTITY LIST → CUSTOMER RESPONSE DTO LIST
    // -------------------------------------------------------------
    public List<CustomerProductVariantSizeResponseDTO> toResponseDTOs(List<ProductVariantSize> sizes) {

        if (sizes == null || sizes.isEmpty()) {
            return Collections.emptyList();
        }

        return sizes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

}
