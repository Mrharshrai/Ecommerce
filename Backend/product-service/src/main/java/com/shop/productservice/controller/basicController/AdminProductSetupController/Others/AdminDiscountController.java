package com.shop.productservice.controller.basicController.AdminProductSetupController.Others;

import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.ApiResponse;
import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.CreateDiscountRequest;
import com.shop.productservice.DTOs.DiscountDTOs.RequestDTOs.UpdateDiscountRequest;
import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.DiscountResponse;
import com.shop.productservice.service.other.DiscountService;
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
@RequestMapping("/api/adminProduct/discounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiscountController {

    private final DiscountService discountService;
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
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(
            @Valid @RequestBody CreateDiscountRequest requestPayload,
            HttpServletRequest request) {
        DiscountResponse discount = discountService.createDiscount(requestPayload);
        String adminEmail = getAdminEmail();

        logActivity(
                discount.getId(),
                ProductActivityType.DISCOUNT_CREATED,
                "Admin '" + adminEmail
                        + "' created discount '"
                        + discount.getDiscountName()
                        + "' (Discount ID: " + discount.getId()
                        + ", Code: " + discount.getDiscountCode() + ").",
                request
        );

        // Using the record we created
        ApiResponse<DiscountResponse> response = ApiResponse.success(
                "Discount created successfully",
                discount
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateDiscountRequest requestPayload,
            HttpServletRequest request) {
        DiscountResponse discount = discountService.updateDiscount(id, requestPayload);
        String adminEmail = getAdminEmail();
        logActivity(
                discount.getId(),
                ProductActivityType.DISCOUNT_UPDATED,
                "Admin '" + adminEmail
                        + "' updated discount '"
                        + discount.getDiscountName()
                        + "' (Discount ID: " + discount.getId()
                        + ", Code: " + discount.getDiscountCode() + ").",
                request
        );

        ApiResponse<DiscountResponse> response = ApiResponse.success(
                "Discount updated successfully",
                discount
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountResponse> getDiscountById(@PathVariable("id") Long id) {
        DiscountResponse discount = discountService.getDiscountById(id);
        return ResponseEntity.ok(discount);
    }

    @GetMapping
    public ResponseEntity<List<DiscountResponse>> getAllDiscounts() {
        // active/inactive/non-deleted
        List<DiscountResponse> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DiscountResponse>> getActiveDiscounts() {
        //only active discounts
        List<DiscountResponse> discounts = discountService.getActiveDiscounts();
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/type/{applyTo}")
    public ResponseEntity<List<DiscountResponse>> getDiscountsByApplyTo(@PathVariable("applyTo") String applyTo) {
        // active/inactive/non-deleted
        List<DiscountResponse> discounts = discountService.getDiscountsByApplyTo(applyTo);
        return ResponseEntity.ok(discounts);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<DiscountResponse>> activateDiscount(@PathVariable("id") Long id
            ,HttpServletRequest request) {
        DiscountResponse discount = discountService.activateDiscount(id);
        String adminEmail = getAdminEmail();

        logActivity(
                discount.getId(),
                ProductActivityType.DISCOUNT_ACTIVATED,
                "Admin '" + adminEmail
                        + "' activated discount '"
                        + discount.getDiscountName()
                        + "' (Discount ID: " + discount.getId()
                        + ", Code: " + discount.getDiscountCode() + ").",
                request
        );

        ApiResponse<DiscountResponse> response = ApiResponse.success(
                "Discount activated successfully",
                discount
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<DiscountResponse>> deactivateDiscount(@PathVariable("id") Long id, HttpServletRequest request,@RequestBody @Valid String reason ) {

        Long targetId = id;
        DiscountResponse discount = discountService.deactivateDiscount(targetId);

        String adminEmail = getAdminEmail();
        String cleanReason = reason.replace("\"", "").trim();

        logActivity(
                targetId,
                ProductActivityType.DISCOUNT_DEACTIVATED,
                "Admin '" + adminEmail
                        + "' deactivated discount '"
                        + discount.getDiscountName()
                        + "' (Discount ID: " + discount.getId()
                        + ", Code: " + discount.getDiscountCode()
                        + "). Reason: " + cleanReason,
                request
        );
        ApiResponse<DiscountResponse> response = ApiResponse.success(
                "Discount deactivated successfully",
                discount
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDiscount(@PathVariable("id") Long id, HttpServletRequest request,@RequestBody @Valid String reason ) {

        DiscountResponse discount = discountService.getDiscountById(id);
        discountService.deleteDiscount(id);
        String adminEmail = getAdminEmail();
        String cleanReason = reason.replace("\"", "").trim();

        logActivity(
                discount.getId(),
                ProductActivityType.DISCOUNT_DELETED,
                "Admin '" + adminEmail
                        + "' deleted discount '"
                        + discount.getDiscountName()
                        + "' (Discount ID: " + discount.getId()
                        + ", Code: " + discount.getDiscountCode()
                        + "). Reason: " + cleanReason,
                request
        );        Map<String, String> response = new HashMap<>();
        response.put("message", "Discount deleted successfully");
        return ResponseEntity.ok(response);
    }

    private String getAdminEmail() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated admin found.");
        }

        return auth.getName();
    }
}
