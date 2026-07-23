package com.shop.productservice.controller.basicController.AdminProductSetupController.Others;

import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityFilterResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityStatsResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivitySummaryResponseDTO;
import com.shop.productservice.service.activityService.ProductActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adminProduct/product-activities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductActivityLogController {

    private final ProductActivityService productActivityService;

    /**
     * =============================================================
     * 1️⃣ Product History (Product Details Page)
     * <p>
     * Includes:
     * • Product
     * • Variant
     * • Size
     * • Image
     * • Related Product
     * =============================================================
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<Page<ProductActivityResponseDTO>> getProductActivityHistory(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                productActivityService.getProductActivityHistory(productId, pageable)
        );
    }

    // 2️⃣ Discount History (Discount Details page)
    // GET /discounts/{discountId}
    //
    // targetId = discountId
    // activityType IN (DISCOUNT_*)
    @GetMapping("/discounts/{discountId}")
    public ResponseEntity<Page<ProductActivityResponseDTO>> getDiscountActivityHistory(
            @PathVariable Long discountId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                productActivityService.getDiscountActivityHistory(discountId, pageable)
        );
    }

    /**
     * =============================================================
     * 3️⃣ Related Product History
     * =============================================================
     */
    @GetMapping("/related-products/{productId}")
    public ResponseEntity<Page<ProductActivityResponseDTO>> getRelatedProductActivityHistory(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                productActivityService.getRelatedProductActivityHistory(productId, pageable)
        );
    }

    /**
     * =============================================================
     * 4️⃣ Recent Activities (Dashboard)
     * =============================================================
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<ProductActivityResponseDTO>> getRecentActivities(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                productActivityService.getRecentActivities(pageable)
        );
    }

    /**
     * =============================================================
     * 5️⃣ Activities by Admin Email
     * =============================================================
     */
    @GetMapping("/admin/{email}")
    public ResponseEntity<Page<ProductActivityResponseDTO>> getAdminActivityHistory(
            @PathVariable String email,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                productActivityService.getAdminActivityHistory(email, pageable)
        );
    }

    /**
     * =============================================================
     * 6️⃣ Activity Dashboard Summary
     * =============================================================
     */
    @GetMapping("/summary")
    public ResponseEntity<ProductActivitySummaryResponseDTO> getActivitySummary() {

        return ResponseEntity.ok(
                productActivityService.getActivitySummary()
        );
    }

    /**
     * =============================================================
     * 7️⃣ Activity Dashboard Statistics
     * =============================================================
     */
    @GetMapping("/statistics")
    public ResponseEntity<ProductActivityStatsResponseDTO> getActivityStatistics() {

        return ResponseEntity.ok(
                productActivityService.getActivityStatistics()
        );
    }

    /**
     * =============================================================
     * 8️⃣ Activity Filter Metadata
     * =============================================================
     */
    @GetMapping("/filters")
    public ResponseEntity<ProductActivityFilterResponseDTO> getActivityFilters() {

        return ResponseEntity.ok(
                productActivityService.getActivityFilters()
        );
    }

}
