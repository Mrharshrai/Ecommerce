package com.shop.productservice.service.activityService;

import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityFilterResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityStatsResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivitySummaryResponseDTO;
import com.shop.productservice.entity.ProductActivityLog;
import com.shop.productservice.enums.ProductActivityType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductActivityService {
    
    /**
     * Log a product activity
     */
    void logActivity(String userEmail, String userRole,
                    Long targetId, ProductActivityType activityType, String description,
                    HttpServletRequest request);
    
    /**
     * Log activity with minimal info (no request)
     */
    void logActivity(String userEmail, String userRole,
                    Long targetId, ProductActivityType activityType, String description);

    /**
     * ============================================================
     * Get complete activity history for a Product.
     *
     * Includes:
     * • Product activities
     * • Variant activities
     * • Size activities
     * • Image activities
     * • Related Product activities
     * ============================================================
     */
    Page<ProductActivityResponseDTO> getProductActivityHistory(
            Long productId,
            Pageable pageable
    );

    /**
     * ============================================================
     * Get activity history for a Discount.
     * ============================================================
     */
    Page<ProductActivityResponseDTO> getDiscountActivityHistory(
            Long discountId,
            Pageable pageable
    );

    /**
     * ============================================================
     * Get activity history for Related Products of a Product.
     *
     * Includes only RELATED_PRODUCT_* activities.
     * ============================================================
     */
    Page<ProductActivityResponseDTO> getRelatedProductActivityHistory(
            Long productId,
            Pageable pageable
    );

    /**
     * ============================================================
     * Get latest activities across the Product Service.
     * ============================================================
     */
    Page<ProductActivityResponseDTO> getRecentActivities(
            Pageable pageable
    );

    /**
     * ============================================================
     * Get activities performed by a specific Admin.
     * ============================================================
     */
    Page<ProductActivityResponseDTO> getAdminActivityHistory(
            String adminEmail,
            Pageable pageable
    );

    /**
     * ============================================================
     * Dashboard Summary
     * ============================================================
     */
    ProductActivitySummaryResponseDTO getActivitySummary();

    /**
     * ============================================================
     * Dashboard Statistics
     * ============================================================
     */
    ProductActivityStatsResponseDTO getActivityStatistics();

    /**
     * ============================================================
     * Get available filters for Activity Logs.
     * ============================================================
     */
    ProductActivityFilterResponseDTO getActivityFilters();

}
