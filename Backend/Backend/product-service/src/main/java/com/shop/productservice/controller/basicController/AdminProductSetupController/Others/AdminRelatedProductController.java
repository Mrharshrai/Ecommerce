package com.shop.productservice.controller.basicController.AdminProductSetupController.Others;

import com.shop.productservice.DTOs.RelatedProductDTOs.RequestDTOs.AddRelatedProductRequest;
import com.shop.productservice.DTOs.RelatedProductDTOs.ResponseDTOs.RelatedProductResponse;
import com.shop.productservice.service.other.RelatedProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.shop.productservice.service.activityService.ProductActivityService;
import com.shop.productservice.enums.ProductActivityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adminRelatedProduct")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRelatedProductController {

    private final RelatedProductService relatedProductService;
    private final ProductActivityService activityService;
    
    private void logActivity(Long targetId, ProductActivityType type, String description, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        String userEmail = auth.getName();
        String userRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user."));

        activityService.logActivity(userEmail, userRole, targetId, type, description, request);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addRelatedProduct(
            @Valid @RequestBody AddRelatedProductRequest requestPayload,
            HttpServletRequest request) {
        RelatedProductResponse response = relatedProductService.addRelatedProduct(requestPayload);

        String adminEmail = getAdminEmail();
        logActivity(
                response.getProductId(),
                ProductActivityType.RELATED_PRODUCT_CREATED,
                "Admin '" + adminEmail
                        + "' added related product variant '"
                        + response.getRelatedVariantName()
                        + "' (Related Variant ID: " + response.getRelatedProductVariantId()
                        + ", SKU: " + response.getRelatedProductVariantSku()
                        + ", Color: " + response.getRelatedProductColor()
                        + ") to Product '"
                        + response.getProductName()
                        + "' (Product ID: " + response.getProductId()
                        + "). Display Order: " + response.getDisplayOrder() + ".",
                request
        );
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Related product added successfully");
        result.put("data", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<RelatedProductResponse>> getManualRelatedProducts(@PathVariable("productId") Long productId) {
        List<RelatedProductResponse> relatedProducts = relatedProductService.getManualRelatedProducts(productId);
        return ResponseEntity.ok(relatedProducts);
    }

    @DeleteMapping("/{productId}/remove/{relatedVariantId}")
    public ResponseEntity<Map<String, String>> removeRelatedProduct(
            @PathVariable("productId") Long productId,
            @PathVariable("relatedVariantId") Long relatedVariantId,
            @RequestBody @Valid String reason,
            HttpServletRequest request) {

        // Fetch details before deletion
        RelatedProductResponse relatedProduct =
                relatedProductService.getRelatedProduct(productId, relatedVariantId);
        relatedProductService.removeRelatedProduct(productId, relatedVariantId);

        String adminEmail = getAdminEmail();
        String cleanReason = reason.replace("\"", "").trim();

        logActivity(
                productId,
                ProductActivityType.RELATED_PRODUCT_DELETED,
                "Admin '" + adminEmail
                        + "' removed related product variant '"
                        + relatedProduct.getRelatedVariantName()
                        + "' (Related Variant ID: " + relatedProduct.getRelatedProductVariantId()
                        + ", SKU: " + relatedProduct.getRelatedProductVariantSku()
                        + ", Color: " + relatedProduct.getRelatedProductColor()
                        + ") from Product '"
                        + relatedProduct.getProductName()
                        + "' (Product ID: " + relatedProduct.getProductId() + ")." +"Reason: " + cleanReason,
                request
        );
        Map<String, String> response = new HashMap<>();
        response.put("message", "Related product removed successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/restore/{relatedVariantId}")
    public ResponseEntity<Map<String, String>> restoreRelatedProduct(
            @PathVariable("productId") Long productId,
            @PathVariable("relatedVariantId") Long relatedVariantId,
            HttpServletRequest request) {

        // Fetch details for activity log
        RelatedProductResponse relatedProduct =
                relatedProductService.getRelatedProduct(productId, relatedVariantId);

        relatedProductService.restoreRelatedProduct(productId, relatedVariantId);

        String adminEmail = getAdminEmail();

        logActivity(
                productId,
                ProductActivityType.RELATED_PRODUCT_RESTORED,
                "Admin '" + adminEmail
                        + "' restored related product variant '"
                        + relatedProduct.getRelatedVariantName()
                        + "' (Related Variant ID: " + relatedProduct.getRelatedProductVariantId()
                        + ", SKU: " + relatedProduct.getRelatedProductVariantSku()
                        + ", Color: " + relatedProduct.getRelatedProductColor()
                        + ") for Product '"
                        + relatedProduct.getProductName()
                        + "' (Product ID: " + relatedProduct.getProductId() + ").",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Related product restored successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/activate/{relatedVariantId}")
    public ResponseEntity<Map<String, Object>> activateRelatedProduct(
            @PathVariable("productId") Long productId,
            @PathVariable("relatedVariantId") Long relatedVariantId,
            HttpServletRequest request) {
        RelatedProductResponse response = relatedProductService.activateRelatedProduct(productId, relatedVariantId);
        String adminEmail = getAdminEmail();

        logActivity(
                response.getProductId(),
                ProductActivityType.RELATED_PRODUCT_ACTIVATED,
                "Admin '" + adminEmail
                        + "' activated related product variant '"
                        + response.getRelatedVariantName()
                        + "' (Related Variant ID: " + response.getRelatedProductVariantId()
                        + ", SKU: " + response.getRelatedProductVariantSku()
                        + ", Color: " + response.getRelatedProductColor()
                        + ") for Product '"
                        + response.getProductName()
                        + "' (Product ID: " + response.getProductId() + ").",
                request
        );
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Related product activated successfully");
        result.put("data", response);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{productId}/deactivate/{relatedVariantId}")
    public ResponseEntity<Map<String, Object>> deactivateRelatedProduct(
            @PathVariable("productId") Long productId,
            @PathVariable("relatedVariantId") Long relatedVariantId,
            HttpServletRequest request) {
        RelatedProductResponse response = relatedProductService.deactivateRelatedProduct(productId, relatedVariantId);
        String adminEmail = getAdminEmail();

        logActivity(
                response.getProductId(),
                ProductActivityType.RELATED_PRODUCT_DEACTIVATED,
                "Admin '" + adminEmail
                        + "' deactivated related product variant '"
                        + response.getRelatedVariantName()
                        + "' (Related Variant ID: " + response.getRelatedProductVariantId()
                        + ", SKU: " + response.getRelatedProductVariantSku()
                        + ", Color: " + response.getRelatedProductColor()
                        + ") for Product '"
                        + response.getProductName()
                        + "' (Product ID: " + response.getProductId() + ").",
                request
        );
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Related product deactivated successfully");
        result.put("data", response);
        return ResponseEntity.ok(result);
    }

    private String getAdminEmail() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated admin found.");
        }

        return auth.getName();
    }
}
