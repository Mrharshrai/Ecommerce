package com.shop.productservice.DTOs.RelatedProductDTOs.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedProductResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long relatedProductVariantId;
    private String relatedVariantName;
    private String relatedProductColor;
    private String relatedProductVariantSku;
    private Integer displayOrder;
    private boolean isActive;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
//    private Instant createdAt;
}
