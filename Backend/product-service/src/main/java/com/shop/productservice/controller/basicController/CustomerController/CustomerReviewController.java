package com.shop.productservice.controller.basicController.CustomerController;

import com.shop.productservice.DTOs.ReviewDTOs.RequestDTOs.CreateReviewRequest;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ProductRatingInfo;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ReviewResponse;
import com.shop.productservice.service.other.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/reviews")
@RequiredArgsConstructor
@Validated
public class CustomerReviewController {

    private final ReviewService reviewService;

    /**
     * POST /api/customer/reviews
     * Creates a new review for a product.
     *
     * Only accessible by authenticated customers.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> addReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName(); // email is the JWT subject
        ReviewResponse review = reviewService.createReview(request, userEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Review added successfully");
        response.put("review", review);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET ALL REVIEWS FOR A PRODUCT (public)
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }

    // GET RATING SUMMARY FOR A PRODUCT (public)
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ProductRatingInfo> getProductRatingInfo(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductRatingInfo(productId));
    }

    // GET SINGLE REVIEW BY ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    // GET MY REVIEWS (authenticated customer only)
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(reviewService.getReviewsByUserEmail(userEmail));
    }
}
