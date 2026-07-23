package com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductResponseDTO {
    private Long productId;
    private String asin;
    private String name;

    // List of fields that were updated in this request
    private List<String> updatedFields;

    private String message;

}

