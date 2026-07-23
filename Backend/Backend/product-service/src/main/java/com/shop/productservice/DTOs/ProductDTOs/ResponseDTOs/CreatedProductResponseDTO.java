package com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedProductResponseDTO {

    private Long id;
    private String asin;
    private String name;
    private String message;
}

