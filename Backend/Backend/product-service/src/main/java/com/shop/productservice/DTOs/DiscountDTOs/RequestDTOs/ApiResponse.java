package com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs;

import com.fasterxml.jackson.annotation.JsonInclude;

// JsonInclude ensures that if 'data' is null, it won't show up in the JSON
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String message,
        T data
) {
    // You can add a static helper to make it even easier to call
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }
}
