package com.shop.productservice.DTOs.SizeDTOs.RequestDTOs;

import com.shop.productservice.enums.Size;
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
public class CreateProductVariantSizeRequestDTO {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotNull(message = "Size is required")
    private Size size; // enum: S, M, L, XL etc.

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "MRP cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "MRP must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "MRP must have up to 10 digits and 2 decimal places, eg. 99999999.99")
    private BigDecimal mrp;

    @NotNull(message = "Weight cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Weight must be greater than 0")
    private BigDecimal weight;

    @NotNull(message = "Length cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Length must be greater than 0")
//    @Digits(integer = 4, fraction = 2, message = "Length must have up to 6 digits and 2 decimal places")
    private BigDecimal length;

    @NotNull(message = "Width cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Width must be greater than 0")
//    @Digits(integer = 4, fraction = 2, message = "Width must have up to 6 digits and 2 decimal places")
    private BigDecimal width;

    @NotNull(message = "Height cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Height must be greater than 0")
//    @Digits(integer = 4, fraction = 2, message = "Height must have up to 6 digits and 2 decimal places")
    private BigDecimal height;
}
