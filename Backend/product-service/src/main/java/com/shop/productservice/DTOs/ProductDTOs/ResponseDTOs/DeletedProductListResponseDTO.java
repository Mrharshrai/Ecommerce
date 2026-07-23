package com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedProductListResponseDTO {
    private Long id;
    private String asin;
    private String name;
    private String brand;
    private String category;
    private boolean isActive;
    private boolean isDeleted;
}
