package com.shop.productservice.controller.basicController.AdminProductSetupController.SizeSetup;

import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.CreateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.UpdateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.CreatedProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.DeletedSizeListResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.UpdateProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.DeletedVariantListResponseDTO;
import com.shop.productservice.service.sizeService.ProductVariantSizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.shop.productservice.service.activityService.ProductActivityService;
import com.shop.productservice.enums.ProductActivityType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/adminProduct/sizes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminVariantSizeController {

    private final ProductVariantSizeService sizeService;
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

    // 1️⃣ CREATE SIZE
    @PostMapping("/createSize")
    public ResponseEntity<CreatedProductVariantSizeResponseDTO> createSize(
            @Valid @RequestBody CreateProductVariantSizeRequestDTO dto,
            HttpServletRequest request) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        //size's size(s/m/l) check  for isActive=true/false and isDeleted=true/false
        // after size creation flags: isActive=true and isDeleted=false
        CreatedProductVariantSizeResponseDTO response= sizeService.createSize(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        logActivity(
                response.getProductId(),
                ProductActivityType.SIZE_CREATED,
                "Admin '" + adminEmail
                        + "' created size (Size ID: " + response.getSizeId()
                        + ", Size SKU: " + response.getSizeSku()
                        + ") for Variant ID " + response.getVariantId()
                        + " under Product ID " + response.getProductId() + ".",
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2️⃣ UPDATE SIZE
    @PutMapping("/updateSize")
    public ResponseEntity<UpdateProductVariantSizeResponseDTO> updateSize(
            @Valid @RequestBody UpdateProductVariantSizeRequestDTO dto,
            HttpServletRequest request) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        //size must exist as isActive=true/false and isDeleted=false
        UpdateProductVariantSizeResponseDTO response = sizeService.updateSize(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        logActivity(
                response.getProductId(),
                ProductActivityType.SIZE_UPDATED,
                "Admin '" + adminEmail
                        + "' updated size '" + response.getSize()
                        + "' (Size ID: " + response.getSizeId()
                        + ") for Variant ID " + response.getVariantId()
                        + " under Product ID " + response.getProductId()
                        + ". Updated fields: "
                        + String.join(", ", response.getUpdatedFields()) + ".",
                request
        );
        return ResponseEntity.ok(response);
    }

    // 3️⃣ GET SIZE BY ID
    @GetMapping("/{sizeId}")
    public ResponseEntity<ProductVariantSizeResponseDTO> getSizeById(@PathVariable("sizeId") Long sizeId) {
        // get only isactive=true/false and isDeleted=false size only
        return ResponseEntity.ok(sizeService.getSizeById(sizeId));
    }

    // 4️⃣ GET SIZE BY SKU
    @GetMapping("/sku/{sizeSku}")
    public ResponseEntity<ProductVariantSizeResponseDTO> getSizeBySku(@PathVariable("sizeSku") String sizeSku) {
        // get only isactive=true/false and isDeleted=false size only
        return ResponseEntity.ok(sizeService.getSizeBySku(sizeSku));
    }

    // 5️⃣ GET DELETED SIZE
    @GetMapping("/deletedSize/{sizeId}")
    public ResponseEntity<ProductVariantSizeResponseDTO> getDeletedSize(@PathVariable("sizeId") Long sizeId) {
        // parent product/variant can be active/inactive/deleted
        // size should be isActive=false and isDeleted=true
        return ResponseEntity.ok(sizeService.getDeletedSize(sizeId));
    }

    // 6️⃣ LIST ALL DELETED SIZES (PAGINATED)
    @GetMapping("/allDeletedSizes")
    public ResponseEntity<Page<DeletedSizeListResponseDTO>> getAllDeletedSizes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // parent product/variant can be active/inactive/deleted
        // size list will be of isActive=false and isDeleted=true sizes
        // order will be latest deleted to older deleted
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(sizeService.getAllDeletedSizes(pageable));
    }

    // 7️⃣ LIST ALL SIZES (NON-DELETED) (PAGINATED)
    @GetMapping("/allSizes")
    public ResponseEntity<Page<ProductVariantSizeResponseDTO>> getAllSizes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // size list will be of isActive=true/false and isDeleted=false sizes
        // order will be latest created to older created
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(sizeService.getAllSizes(pageable));
    }

    // 8️⃣ LIST SIZES UNDER VARIANT
    @GetMapping("/sizesUnderVariant/{variantId}")
    public ResponseEntity<List<ProductVariantSizeResponseDTO>> getSizesByVariantId(@PathVariable("variantId") Long variantId) {
        // parent varint must be isActive=true/false and isDeleted=false
        // size list will be of isActive=true/false and isDeleted=false sizes
        // order will be latest created to older created
        return ResponseEntity.ok(sizeService.getSizesByVariantId(variantId));
    }

    // 9️⃣ DEACTIVATE SIZE
    @PutMapping("/deactivateSize/{sizeId}")
    public ResponseEntity<String> deactivateSize(@PathVariable("sizeId") Long sizeId, HttpServletRequest request, @RequestBody @Valid String reason) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        // size must be isActive=true and isDeleted=false
        String response=sizeService.deactivateSize(sizeId, request, reason);
        //Activity logs in service layer
        return ResponseEntity.ok(response);
    }

    // 🔟 ACTIVATE SIZE
    @PutMapping("/activateSize/{sizeId}")
    public ResponseEntity<String> activateSize(@PathVariable("sizeId") Long sizeId, HttpServletRequest request) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        // size must be isActive=false and isDeleted=false
        String response=sizeService.activateSize(sizeId, request);
        //Activity logs in service layer
        return ResponseEntity.ok(response);
    }

    // 1️⃣1️⃣ SOFT DELETE SIZE
    @DeleteMapping("/deleteSize/{sizeId}")
    public ResponseEntity<String> deleteSize(@PathVariable("sizeId") Long sizeId, HttpServletRequest request, @RequestBody @Valid String reason) {
        // size should be isActive=true/false and isDeleted=flase
        // parent product/variant should be isActive=true/false and isDeleted=flase
        String response = sizeService.deleteSize(sizeId, request,reason);
        //Activity logs in service layer
        return ResponseEntity.ok(response);
    }

    // 1️⃣2️⃣ RESTORE SIZE
    @PutMapping("/restoreSize/{sizeId}")
    public ResponseEntity<String> restoreSize(@PathVariable("sizeId") Long sizeId, HttpServletRequest request) {
        // size should be isActive=false and isDeleted=true
        // parent product/variant should be isActive=true/false and isDeleted=flase
        // conflict will never happen, as creations checking size existed as A/I/D
        String response = sizeService.restoreSize(sizeId, request);
        //Activity logs in service layer
        return ResponseEntity.ok(response);
    }

    // 1️⃣3️⃣ COUNT SIZES UNDER A VARIANT
    @GetMapping("/sizeUnderVariant/{variantId}/count")
    public ResponseEntity<Long> countSizesByVariantId(@PathVariable Long variantId) {
        return ResponseEntity.ok(sizeService.countsizesByVariantId(variantId));
    }

    // Get all deleted sizes under a variant.
    @GetMapping("/variant/{variantId}/deletedSizes")
    public ResponseEntity<Page<DeletedSizeListResponseDTO>>
    getDeletedSizseByVariantId(@PathVariable("variantId") Long variantId, Pageable pageable) {
        // parent product should be isActive=true/false and isDeleted=true/flase
        // will get list of only deleted sizes under active/inactive/deleted variant
        return ResponseEntity.ok(
                sizeService.getDeletedSizesByVariantId(
                        variantId,
                        pageable
                )
        );
    }

}
