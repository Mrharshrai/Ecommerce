package com.shop.productservice.DTOs.ReviewDTOs.RequestDTOs;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 255, message = "Review title must be less than 255 characters")
    private String reviewTitle;

    @Size(max = 2000, message = "Review text must be less than 2000 characters")
    private String reviewText;

    // Optional: up to 4 image URLs (uploaded separately via the file-upload service)
    @Size(max = 4, message = "You can attach a maximum of 4 images per review")
    private List<@NotNull(message = "Image URL must not be null") String> reviewImageUrls;
}
