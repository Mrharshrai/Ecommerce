package com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String userEmail;
    private Long orderId;
    private Integer rating;
    private String reviewTitle;
    private String reviewText;
    private boolean verifiedPurchase;
    private List<String> reviewImageUrls;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;
}
