package com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInventoryResponse {

    private Long id;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String warehouseCity;
    private Long variantSizeId;
    private String sizeSku;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;
}
