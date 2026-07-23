package com.shop.productservice.controller.basicController.AdminProductSetupController.VariantSetup;

import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.CreateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.UpdateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.CreatedProductVariantResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.DeletedVariantListResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.UpdateProductVariantResponseDTO;
import com.shop.productservice.enums.ProductActivityType;
import com.shop.productservice.service.activityService.ProductActivityService;
import com.shop.productservice.service.variantService.ProductVariantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adminProduct/variants")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminVariantController {

    private final ProductVariantService variantService;
    private final ProductActivityService activityService;

    private void logActivity(Long productId, ProductActivityType type, String description, HttpServletRequest request) {
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

        activityService.logActivity(userEmail, userRole, productId, type, description, request);
    }

    // CREATE VARIANT
    @PostMapping("/createVariant")
    public ResponseEntity<CreatedProductVariantResponseDTO> createVariant(
            @Valid @RequestBody CreateProductVariantRequestDTO dto,
            HttpServletRequest request) {
        // parent product should be isActive=true/false and isDeleted=false
        //variant's colour check for isActive=true/false and isDeleted=true/false
        // after variant creation flags: isActive=true and isDeleted=false
        CreatedProductVariantResponseDTO response = variantService.createVariant(dto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        logActivity(
                response.getProductId(),
                ProductActivityType.VARIANT_CREATED,
                "Admin '" + adminEmail
                        + "' created variant '" + response.getVariantName()
                        + "' (Variant ID: " + response.getVariantId()
                        + ", SKU: " + response.getSkuCode()
                        + ") for Product ID " + response.getProductId() + ".",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UPDATE
    @PutMapping("/updateVariant")
    public ResponseEntity<UpdateProductVariantResponseDTO> updateVariant(
            @Valid @RequestBody UpdateProductVariantRequestDTO dto,
            HttpServletRequest request) {
        // variant should be isActive=true/false and isDeleted=false
        // parent product should be isActive=true/false and isDeleted=false
        UpdateProductVariantResponseDTO response = variantService.updateVariant(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        logActivity(
                response.getProductId(),
                ProductActivityType.VARIANT_UPDATED,
                "Admin '" + adminEmail
                        + "' updated variant '" + response.getVariantName()
                        + "' (Variant ID: " + response.getVariantId()
                        + ", SKU: " + response.getSkuCode()
                        + ") for Product ID " + response.getProductId()
                        + ". Updated fields: "
                        + String.join(", ", response.getUpdatedFields()) + ".",
                request
        );
        return ResponseEntity.ok(response);
    }

    // GET BY ID
    @GetMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponseDTO> getVariantById(@PathVariable("variantId") Long variantId) {
        // variant should be isActive=true/false and isDeleted=false
        // parent product should be isActive=true/false and isDeleted=false
        // only will get list of isActive=true/false and isDeleted=false images/sizes
        return ResponseEntity.ok(variantService.getVariantById(variantId));
    }

    // GET BY SKU
    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<ProductVariantResponseDTO> getVariantBySku(@PathVariable("skuCode") String skuCode) {
        // variant should be isActive=true/false and isDeleted=false
        // parent product should be isActive=true/false and isDeleted=false
        // only will get list of isActive=true/false and isDeleted=false images/sizes
        return ResponseEntity.ok(variantService.getVariantBySkuCode(skuCode));
    }

    // ALL variants
    @GetMapping("/getAllVariants")
    public ResponseEntity<Page<ProductVariantResponseDTO>> getAllVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // variant should be isActive=true/false and isDeleted=false
        // only will get list of isActive=true/false and isDeleted=false images/sizes
        // order will be latest created to older created
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(variantService.getAllVariants(pageable));
    }

    // ALL variants under product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantResponseDTO>> getVariantsByProductId(@PathVariable("productId") Long productId) {
        // variant should be isActive=true/false and isDeleted=false
        // parent product should be isActive=true/false and isDeleted=false
        // only will get list of isActive=true/false and isDeleted=false images/sizes
        // order will be latest created to older created
        return ResponseEntity.ok(variantService.getVariantsByProductId(productId));
    }

    // DELETED VARIANT BY ID
    @GetMapping("/deletedVariant/{variantId}")
    public ResponseEntity<ProductVariantResponseDTO> getDeletedVariantById(@PathVariable("variantId") Long variantId) {
        // variant should be isActive=false and isDeleted=true
        // parent product can be active/inactive/deleted
        // only will get list of isActive=false and isDeleted=true images/sizes
        return ResponseEntity.ok(variantService.getDeletedVariantById(variantId));
    }

    // ALL DELETED VARIANTS (PAGINATED)
    @GetMapping("/deletedVariants")
    public ResponseEntity<Page<DeletedVariantListResponseDTO>> getDeletedVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // parent product can be active/inactive/deleted
        // variant should be isActive=false and isDeleted=true
        // only will get list of isActive=false and isDeleted=true images/sizes
        // order will be latest deleted to older deleted
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(variantService.getDeletedVariants(pageable));
    }

    //DEACTIVATE VARIANT
    @PutMapping("/deactivateVariant/{variantId}")
    public ResponseEntity<String> deactivateVariant(@PathVariable("variantId") Long variantId, HttpServletRequest request,
                                                    @RequestBody @Valid String reason) {
        // variant should be isActive=true and isDeleted=flase
        // parent product should be isActive=true/false and isDeleted=flase
        String response = variantService.deactivateVariant(variantId, reason, request);
        //activity logs is logged in service, due to repo calling to get product id
        return ResponseEntity.ok(response);
    }

    // ACTIVATE VARIANT
    @PutMapping("/activateVariant/{variantId}")
    public ResponseEntity<String> activateVariant(@PathVariable("variantId") Long variantId, HttpServletRequest request) {
        // variant should be isActive=false and isDeleted=flase
        // parent product should be isActive=true/false and isDeleted=flase
        String response = variantService.activateVariant(variantId, request);
        //activity logs is logged in service, due to repo calling to get product id
        return ResponseEntity.ok(response);
    }

    // DELETE VARIANT(soft delete)
    @DeleteMapping("/deleteVariant/{variantId}")
    public ResponseEntity<String> deleteVariant(@PathVariable("variantId") Long variantId, HttpServletRequest request,
                                                @RequestBody @Valid String reason) {
        // variant should be isActive=true/false and isDeleted=flase
        // parent product should be isActive=true/false and isDeleted=flase
        String response = variantService.deleteVariant(variantId, request, reason);
        //activity logs is logged in service, due to repo calling to get product id
        return ResponseEntity.ok(response);
    }

    // RESTORE VARIANT
    @PutMapping("/restoreVariant/{variantId}")
    public ResponseEntity<String> restoreVariant(@PathVariable("variantId") Long variantId, HttpServletRequest request) {
        // variant should be isActive=false and isDeleted=true
        // parent product should be isActive=true/false and isDeleted=flase
        // colour conflict will never happen, as creation itself checking colour in existed active/inactive /deleted records
        String response = variantService.restoreVariant(variantId, request);
        //activity logs is logged in service, due to repo calling to get product id
        return ResponseEntity.ok(response);
    }

    //Get all deleted variants under a Product.
    @GetMapping("/product/{productId}/deletedVariants")
    public ResponseEntity<Page<DeletedVariantListResponseDTO>>
    getDeletedVariantsByProductId(@PathVariable("productId") Long productId, Pageable pageable) {
        // parent product should be isActive=true/false and isDeleted=true/flase
        // will get list of only deleted variants under active/inactive/deleted product
        return ResponseEntity.ok(
                variantService.getDeletedVariantsByProductId(
                        productId,
                        pageable
                )
        );
    }

}
