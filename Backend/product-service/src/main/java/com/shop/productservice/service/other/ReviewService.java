package com.shop.productservice.service.other;

import com.shop.productservice.DTOs.ReviewDTOs.RequestDTOs.CreateReviewRequest;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ProductRatingInfo;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ReviewResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * Creates a review.
     * @param request   review payload (rating, title, text, orderId, productId, up to 4 image URLs)
     * @param userEmail authenticated customer's email
     */
    ReviewResponse createReview(CreateReviewRequest request, String userEmail);

    ReviewResponse getReviewById(Long id);

    List<ReviewResponse> getReviewsByProductId(Long productId);

    Page<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable);

    List<ReviewResponse> getReviewsByUserEmail(String userEmail);

    ProductRatingInfo getProductRatingInfo(Long productId);

    void deleteReview(Long id);

    void updateProductRating(Long productId);
}
