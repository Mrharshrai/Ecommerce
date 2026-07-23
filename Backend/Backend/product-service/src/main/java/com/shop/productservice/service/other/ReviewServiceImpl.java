package com.shop.productservice.service.other;

import com.shop.productservice.DTOs.ReviewDTOs.RequestDTOs.CreateReviewRequest;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ProductRatingInfo;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ReviewResponse;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.Review;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    /**
     * Creates a new product review with optional image URLs (up to 4).
     * Prevents duplicate reviews (same user + product + order) and auto-updates product rating.
     *
     * @param request   - Contains review details (rating, title, text, orderId, image URLs)
     * @param userEmail - Email of the authenticated user (extracted from JWT subject)
     * @return ReviewResponse with created review information including image URLs
     */
    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, String userEmail) {
        // Verify product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Check if user has already reviewed this product
        if (reviewRepository.existsByUserEmailAndProductIdAndIsDeletedFalse(userEmail, request.getProductId())) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }

        // Resolve image URLs from request (validated by @Size(max=4) on the DTO)
        List<String> imageUrls = request.getReviewImageUrls() != null
                ? request.getReviewImageUrls()
                : new ArrayList<>();

        // Create review entity
        Review review = Review.builder()
                .product(product)
                .userEmail(userEmail)
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .reviewTitle(request.getReviewTitle())
                .reviewText(request.getReviewText())
                .isVerifiedPurchase(true)
                .reviewImages(imageUrls)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Auto-update product's average rating and review count
        updateProductRating(request.getProductId());

        return mapToResponse(savedReview);
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Review not found"));
        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdAndIsDeletedFalse(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        // non deleted product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return reviewRepository.findByProductIdAndIsDeletedFalse(productId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<ReviewResponse> getReviewsByUserEmail(String userEmail) {
        return reviewRepository.findByUserEmailAndIsDeletedFalse(userEmail).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingInfo getProductRatingInfo(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long reviewCount = reviewRepository.countByProductId(productId);
        List<Object[]> distribution = reviewRepository.getRatingDistributionByProductId(productId);

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }

        return ProductRatingInfo.builder()
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null)
                .reviewCount(reviewCount)
                .ratingDistribution(ratingDistribution)
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Review not found"));

        Long productId = review.getProduct().getId();

        // Soft delete — preserve record for audit/analytics
        review.setDeleted(true);
        reviewRepository.save(review);

        // Recalculate product rating excluding this review
        updateProductRating(productId);
    }

    @Override
    @Transactional
    public void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long reviewCount = reviewRepository.countByProductId(productId);

        product.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
        product.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);

        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userEmail(review.getUserEmail())
                .orderId(review.getOrderId())
                .rating(review.getRating())
                .reviewTitle(review.getReviewTitle())
                .reviewText(review.getReviewText())
                .verifiedPurchase(review.isVerifiedPurchase())
                .reviewImageUrls(review.getReviewImages())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
