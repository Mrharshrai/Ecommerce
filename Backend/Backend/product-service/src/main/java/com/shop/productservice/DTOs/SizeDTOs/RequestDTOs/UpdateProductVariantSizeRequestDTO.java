package com.shop.productservice.DTOs.SizeDTOs.RequestDTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductVariantSizeRequestDTO {

    @NotNull(message = "Size ID is required")
    private Long sizeId;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @DecimalMin(value = "0.01", inclusive = true, message = "MRP must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "MRP must have up to 10 digits and 2 decimal places, eg. 99999999.99")
    private BigDecimal mrp;

    @DecimalMin(value = "0.01", inclusive = true, message = "Weight must be greater than 0")
    private BigDecimal weight;

    @DecimalMin(value = "0.01", inclusive = true, message = "Length must be greater than 0")
    private BigDecimal length;

    @DecimalMin(value = "0.01", inclusive = true, message = "Width must be greater than 0")
    private BigDecimal width;

    @DecimalMin(value = "0.01", inclusive = true, message = "Height must be greater than 0")
    private BigDecimal height;
}
