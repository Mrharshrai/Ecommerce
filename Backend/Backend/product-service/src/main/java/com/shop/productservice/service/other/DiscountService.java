package com.shop.productservice.service.other;

import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.CreateDiscountRequest;
import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.UpdateDiscountRequest;
import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.DiscountResponse;
import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.PricingInfo;
import com.shop.productservice.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {

    DiscountResponse createDiscount(CreateDiscountRequest request);

    DiscountResponse updateDiscount(Long id, UpdateDiscountRequest request);

    DiscountResponse getDiscountById(Long id);

    List<DiscountResponse> getAllDiscounts();

    List<DiscountResponse> getActiveDiscounts();

    List<DiscountResponse> getDiscountsByApplyTo(String applyTo);

    DiscountResponse activateDiscount(Long id);

    DiscountResponse deactivateDiscount(Long id);

    void deleteDiscount(Long id);

    PricingInfo calculatePricing(Product product, BigDecimal basePrice);
}
