package com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDiscountRequest {

    @Size(max = 255, message = "Discount name must be less than 255 characters")
    @Pattern(regexp = ".*\\S.*", message = "Discount name cannot be blank")
    private String discountName;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Discount value can have total 10 digits(12345678.12) at most 8 interger digits and  2 decimal places only")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "Minimum product price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Minimum product price can have total 10 digits at most 8 integer digits and 2 decimal places only")
    private BigDecimal minProductPrice;

    private Instant startDate;

    private Instant endDate;

    @AssertTrue(message = "End date must be after start date")
    public boolean isValidTimeRange() {
        // If either is null, we pass DTO validation.
        // The service layer will handle the cross-check with existing data.
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
