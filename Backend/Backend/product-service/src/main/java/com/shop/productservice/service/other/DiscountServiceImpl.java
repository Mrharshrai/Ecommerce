package com.shop.productservice.service.other;

import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.CreateDiscountRequest;
import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.UpdateDiscountRequest;
import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.DiscountResponse;
import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.PricingInfo;
import com.shop.productservice.entity.Discount;
import com.shop.productservice.entity.Product;
import com.shop.productservice.enums.DiscountApplyTo;
import com.shop.productservice.enums.DiscountType;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.repository.DiscountRepository;
import com.shop.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    /**
     * Creates a new discount (category-wise or product-specific)
     * Validates discount code uniqueness and request parameters
     *
     * @param request - Contains discount details (code, type, value, dates, etc.)
     * @return DiscountResponse with created discount information
     */
    @Override
    @Transactional
    public DiscountResponse createDiscount(CreateDiscountRequest request) {

        // ✅ ADD: Normalize before ANY check or save
        // Trims whitespace, uppercases — "save10 " and "Save10" both become "SAVE10"
        String normalizedCode = request.getDiscountCode().trim().toUpperCase();

        // Validate discount parameters (dates, values, etc.)
        validateDiscountRequest(request);

        // Check if discount code already exists as active/inactive/deleted
        Optional<Discount> existingRecord = Optional.ofNullable(
                discountRepository.findByCodeIgnoreDeletedFilter(normalizedCode)
        );

        if (existingRecord.isPresent()) {
            Discount d = existingRecord.get();
            if (d.isDeleted()) {
                throw new IllegalArgumentException("This code already exists in your deleted discounts. Please use a different code or restore the old one.");
            } else {
                throw new IllegalArgumentException("Discount code '" + request.getDiscountCode().trim().toUpperCase() + "' is already in use.");
            }
        }

        // Build discount entity
        Discount discount = Discount.builder()
                .discountCode(request.getDiscountCode().trim().toUpperCase())
                .discountName(request.getDiscountName().trim().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
//                .minProductPrice(request.getMinProductPrice())
                .minProductPrice(
                        request.getDiscountType() == DiscountType.FLAT
                                ? request.getMinProductPrice()
                                : null
                )
                .applyTo(request.getApplyTo())
//                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();

        // 4. Handle Conditional Application (Product vs Category)
        configureDiscountTargets(discount, request);

        try {
            // Save and Flush to catch DataIntegrityViolation within the try block
            Discount savedDiscount = discountRepository.saveAndFlush(discount);
            return mapToResponse(savedDiscount);
        } catch (DataIntegrityViolationException e) {
            // This catches the race condition if two people use the same code simultaneously
            throw new IllegalArgumentException("Discount code already exists (concurrent update).");
        }
    }

    private void validateDiscountRequest(CreateDiscountRequest request) {

        // Type-specific logic (Using .equals for Null-Safety)
        if (DiscountType.PERCENTAGE.equals(request.getDiscountType())) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Percentage discount cannot exceed 100%.");
            }
        } else if (DiscountType.FLAT.equals(request.getDiscountType())) {
            if (request.getMinProductPrice() == null) {
                throw new IllegalArgumentException("Minimum product price is required for FLAT discounts.");
            }
            if (request.getMinProductPrice().compareTo(request.getDiscountValue()) <= 0) {
                throw new IllegalArgumentException("Minimum product price must be strictly greater than the flat discount value.");
            }
        }

        // Strict ApplyTo Validation
        if (DiscountApplyTo.CATEGORY.equals(request.getApplyTo())) {
            if (request.getCategory() == null || request.getCategory().isBlank()) {
                throw new IllegalArgumentException("Category name is required for category discounts");
            }
        } else if (DiscountApplyTo.PRODUCT.equals(request.getApplyTo())) {
            if (request.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for product discounts");
            }
        }
    }

    private void configureDiscountTargets(Discount discount, CreateDiscountRequest request) {
        switch (request.getApplyTo()) {
            case PRODUCT -> {
                // ✅ Validate and attach product
                Product product = productRepository.findById(request.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + request.getProductId()));

                // ✅ Block duplicate active product discount
//                if (discountRepository.existsActiveProductDiscount(product.getId(), now)) {
//                    throw new IllegalArgumentException(
//                            "Product (ID: " + product.getId() + ") already has an active discount. " +
//                                    "Please deactivate or delete the existing discount before creating a new one."
//                    );
//                }

                discount.setProduct(product);
                discount.setCategory(null); // explicit clean
            }
            case CATEGORY -> {
                // ✅ Block duplicate active category discount
//                if (discountRepository.existsActiveCategoryDiscount(request.getCategory().trim().toUpperCase(), now)) {
//                    throw new IllegalArgumentException(
//                            "Category '" + request.getCategory().trim().toUpperCase() + "' already has an active discount. " +
//                                    "Please deactivate or delete the existing discount before creating a new one."
//                    );
//                }

                // ✅ Attach category, no product
                discount.setCategory(request.getCategory().trim().toUpperCase());
                discount.setProduct(null); // explicit clean
            }
//            case GLOBAL -> {
//                // ✅ ADD: Global applies to everything — no product or category needed
//                // Without this, GLOBAL falls into else and may accidentally store category
//                discount.setProduct(null);
//                discount.setCategory(null);
//            }
            default -> throw new IllegalArgumentException(
                    "Unsupported applyTo type: " + request.getApplyTo()
            );
        }
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long id, UpdateDiscountRequest request) {
        Instant now = Instant.now();
        // check for active/inactive discounts only, deleted filtered out
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Discount not found"));

        // 2. Determine what the final values WILL be after the update.
        // If the request provides a new value, we use it; otherwise, we retain the current DB value.
        BigDecimal targetDiscountValue = (request.getDiscountValue() != null)
                ? request.getDiscountValue()
                : discount.getDiscountValue();

        BigDecimal targetMinProductPrice = (request.getMinProductPrice() != null)
                ? request.getMinProductPrice()
                : discount.getMinProductPrice();

        Instant targetStartDate = (request.getStartDate() != null)
                ? request.getStartDate()
                : discount.getStartDate();

        Instant targetEndDate = (request.getEndDate() != null)
                ? request.getEndDate()
                : discount.getEndDate();

        // 1. THE GRAVEYARD RULE: If a discount is already completely expired in the DB, lock it.
        // Past discounts should stay frozen for historical reporting/order audit trails.
        if (discount.getEndDate().isBefore(now)) {
            throw new IllegalArgumentException("Cannot update an expired discount.");
        }

        // Check A: PERCENTAGE validation
        if (DiscountType.PERCENTAGE.equals(discount.getDiscountType())) {
            if (targetDiscountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("This is a Percentage discount, discount value cannot exceed 100%.");
            }
        }

        // Check B: FLAT validation (Requires minimum product price setup and strict threshold check)
        if (DiscountType.FLAT.equals(discount.getDiscountType())) {
//            if (targetMinProductPrice == null) {
//                throw new IllegalArgumentException("Minimum product price is required for FLAT discounts.");
//            }
            if (targetMinProductPrice.compareTo(targetDiscountValue) <= 0) {
                throw new IllegalArgumentException("Minimum product price (" + targetMinProductPrice
                        + ") must be strictly greater than the flat discount value (" + targetDiscountValue + ").");
            }
        }

        // Check C: Ensure chronologically valid dates across current state + request state
        // 2. THE LIVE START-DATE LOCK: If the discount is currently ACTIVE (live)...
        if (!discount.getStartDate().isAfter(now) && discount.getEndDate().isAfter(now)) {
            // ...and the admin is actively trying to change the start date to something else
            if (request.getStartDate() != null && !request.getStartDate().truncatedTo(ChronoUnit.SECONDS).equals(discount.getStartDate().truncatedTo(ChronoUnit.SECONDS))) {
                throw new IllegalArgumentException("Cannot change the start date of an already active discount.");
            }
        }

        // Check D: If dates are being changed, prevent shifting them into the past relative to execution time
        if (request.getStartDate() != null
                && !request.getStartDate().truncatedTo(ChronoUnit.SECONDS)
                .equals(discount.getStartDate().truncatedTo(ChronoUnit.SECONDS)) // Only check if it's a NEW date
                && request.getStartDate().isBefore(now)) {
            throw new IllegalArgumentException("New start date cannot be in the past.");
        }

        if (request.getEndDate() != null
                && !request.getEndDate().truncatedTo(ChronoUnit.SECONDS)
                .equals(discount.getEndDate().truncatedTo(ChronoUnit.SECONDS)) // Only check if it's a NEW date
                && request.getEndDate().isBefore(now)) {
            throw new IllegalArgumentException("New end date cannot be in the past.");
        }

        if (targetEndDate.isBefore(targetStartDate) || targetEndDate.equals(targetStartDate)) {
            throw new IllegalArgumentException("End date must be strictly after the start date.");
        }

        // MUTATION / APPLYING CHANGES TO ENTITY

        if (request.getDiscountName() != null) {
            discount.setDiscountName(request.getDiscountName().trim().toUpperCase());
        }

        // Apply validated targets to the entity
        discount.setDiscountValue(targetDiscountValue);
        discount.setMinProductPrice(discount.getDiscountType() == DiscountType.FLAT? targetMinProductPrice : null);
        discount.setStartDate(targetStartDate);
        discount.setEndDate(targetEndDate);

        // 4. Persist changes to database (Triggers optimistic locking via @Version if modified concurrently)
        Discount updatedDiscount = discountRepository.save(discount);

        // 5. Transform entity back to standard DTO response
        return mapToResponse(updatedDiscount);
    }

    @Override
    public DiscountResponse getDiscountById(Long id) {
        // filter deleted discounts
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Discount not found"));
        return mapToResponse(discount);
    }

    @Override
    public List<DiscountResponse> getAllDiscounts() {
        // filter deleted discounts
        return discountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiscountResponse> getActiveDiscounts() {
        return discountRepository.findByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiscountResponse> getDiscountsByApplyTo(String applyTo) {
        DiscountApplyTo type;
        try {
            type = DiscountApplyTo.valueOf(applyTo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid applyTo type: '" + applyTo + "'. Valid values are: " + java.util.Arrays.toString(DiscountApplyTo.values()));
        }
        return discountRepository.findByApplyToAndIsDeletedFalse(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiscountResponse activateDiscount(Long id) {
        //active/inactive check
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Discount not found"));
        if (discount.isActive()) {
            throw new IllegalArgumentException("Discount is already active.");
        }
        discount.setActive(true);
        Discount updatedDiscount = discountRepository.save(discount);
        return mapToResponse(updatedDiscount);
    }

    @Override
    @Transactional
    public DiscountResponse deactivateDiscount(Long id) {
        //active/inactive check
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Discount not found"));

        if (!discount.isActive()) {
            throw new IllegalArgumentException("Discount is already inactive.");
        }
        discount.setActive(false);
        Discount updatedDiscount = discountRepository.save(discount);
        return mapToResponse(updatedDiscount);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        //active/inactive check
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Discount not found"));
        discount.setDeleted(true);
        discount.setActive(false);
        discountRepository.save(discount);
    }

    @Override
    public PricingInfo calculatePricing(Product product, BigDecimal basePrice) {
        Instant now = Instant.now();

        List<Discount> productDiscounts = discountRepository.findActiveProductDiscounts(product.getId(), now);
        List<Discount> categoryDiscounts = discountRepository.findActiveCategoryDiscounts(product.getCategory(), now);

        Discount maxDiscount = java.util.stream.Stream.concat(
                        productDiscounts != null ? productDiscounts.stream() : java.util.stream.Stream.empty(),
                        categoryDiscounts != null ? categoryDiscounts.stream() : java.util.stream.Stream.empty()
                )
                .filter(d -> d.getMinProductPrice() == null || basePrice.compareTo(d.getMinProductPrice()) >= 0)
                .max((d1, d2) -> getDiscountAmountForComparison(basePrice, d1).compareTo(getDiscountAmountForComparison(basePrice, d2)))
                .orElse(null);

        if (maxDiscount != null && getDiscountAmountForComparison(basePrice, maxDiscount).compareTo(BigDecimal.ZERO) > 0) {
            return applyDiscount(basePrice, maxDiscount);
        }

        return PricingInfo.builder()
                .mrp(basePrice)
                .sellingPrice(basePrice)
                .discountAmount(BigDecimal.ZERO)
                .discountPercent(0)
                .hasDiscount(false)
                .build();
    }

    private BigDecimal getDiscountAmountForComparison(BigDecimal mrp, Discount discount) {
        if (DiscountType.PERCENTAGE.equals(discount.getDiscountType())) {
            return mrp.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal val = discount.getDiscountValue();
            return mrp.compareTo(val) < 0 ? mrp : val;
        }
    }

    private PricingInfo applyDiscount(BigDecimal mrp, Discount discount) {
        BigDecimal discountAmount;
        BigDecimal sellingPrice;

        if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = mrp.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            sellingPrice = mrp.subtract(discountAmount);
        } else {
            discountAmount = discount.getDiscountValue();
            sellingPrice = mrp.subtract(discountAmount);
            if (sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
                sellingPrice = BigDecimal.ZERO;
            }
        }

        int discountPercent = mrp.compareTo(BigDecimal.ZERO) > 0
                ? discountAmount.multiply(BigDecimal.valueOf(100))
                .divide(mrp, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        return PricingInfo.builder()
                .mrp(mrp)
                .sellingPrice(sellingPrice)
                .discountAmount(discountAmount)
                .discountPercent(discountPercent)
                .hasDiscount(true)
                .discountName(discount.getDiscountName())
                .discountCode(discount.getDiscountCode())
                .build();
    }


    private DiscountResponse mapToResponse(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .discountCode(discount.getDiscountCode())
                .discountName(discount.getDiscountName())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .minProductPrice(discount.getMinProductPrice())
                .applyTo(discount.getApplyTo())
                .category(discount.getCategory())
                .productId(discount.getProduct() != null ? discount.getProduct().getId() : null)
                .productName(discount.getProduct() != null ? discount.getProduct().getName() : null)
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .isActive(discount.isActive())
                .isCurrentlyValid(discount.isCurrentlyValid(Instant.now()))
                .maxUsageCount(discount.getMaxUsageCount())   // null = unlimited
                .usedCount(discount.getUsedCount())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }

//    @Override public List<DiscountResponse> getDiscountsByType(String applyTo) { return List.of(); }
//    @Override @Transactional public DiscountResponse activateDiscount(Long id) { return null; }
//    @Override @Transactional public DiscountResponse deactivateDiscount(Long id) { return null; }
}
