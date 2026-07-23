package com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shop.productservice.enums.DiscountApplyTo;
import com.shop.productservice.enums.DiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountResponse {

    private Long id;
    private String discountCode;
    private String discountName;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minProductPrice;
    private DiscountApplyTo applyTo;
    private String category;
    private Long productId;
    private String productName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant endDate;
    private boolean isActive;
    private boolean isCurrentlyValid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    private Integer maxUsageCount;

    private Integer usedCount;
}
