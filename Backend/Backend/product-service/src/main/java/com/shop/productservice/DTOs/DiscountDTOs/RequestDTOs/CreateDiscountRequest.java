package com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs;

import com.shop.productservice.enums.DiscountApplyTo;
import com.shop.productservice.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDiscountRequest {

    @NotBlank(message = "Discount code is required")
    @Size(max = 50, message = "Discount code must be less than 50 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9]+$",
            message = "Discount code can contain only letters and numbers without spaces or special characters"
    )
    private String discountCode;

    @NotBlank(message = "Discount name is required")
    @Size(max = 255, message = "Discount name must be less than 255 characters")
    private String discountName;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Discount value can have total 10 digits(12345678.12) at most 8 interger digits and  2 decimal places only")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "Minimum product price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Minimum product price can have total 10 digits at most 8 integer digits and 2 decimal places only")
    private BigDecimal minProductPrice;

    @NotNull(message = "Apply to is required")
    private DiscountApplyTo applyTo;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    private Long productId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private Instant startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private Instant endDate;

    /**
     * Custom validation check to ensure end date is after start date.
     * You can call this in your service layer before saving.
     */
    @AssertTrue(message = "End date must be after start date")
    public boolean isValidTimeRange() {
        return startDate != null && endDate != null && endDate.isAfter(startDate);
    }
}
