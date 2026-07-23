package com.shop.productservice.controller.basicController.AdminProductSetupController.Others;

import com.shop.productservice.service.other.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.shop.productservice.service.activityService.ProductActivityService;
import com.shop.productservice.enums.ProductActivityType;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs.ReviewResponse;

@RestController
@RequestMapping("/api/adminProduct/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final ProductActivityService activityService;
    
    private void logActivity(Long productId, ProductActivityType type, String description, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth != null ? auth.getName() : "system";
        String userRole = (auth != null && !auth.getAuthorities().isEmpty()) 
            ? auth.getAuthorities().iterator().next().getAuthority() : "ROLE_ADMIN";
        
        activityService.logActivity(userEmail, userRole, productId, type, description, request);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByProductId(
            @PathVariable("productId")  Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable("id") Long id, HttpServletRequest request) {
        reviewService.deleteReview(id);
        logActivity(null, ProductActivityType.REVIEW_DELETED, "Deleted review with ID " + id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Review deleted successfully");
        return ResponseEntity.ok(response);
    }
}
