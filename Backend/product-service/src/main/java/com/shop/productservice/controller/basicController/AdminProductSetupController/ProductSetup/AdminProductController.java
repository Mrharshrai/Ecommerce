package com.shop.productservice.controller.basicController.AdminProductSetupController.ProductSetup;

import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.CreateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.UpdateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.*;
import com.shop.productservice.service.productService.ProductService;
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

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/adminProduct/products")
@PreAuthorize("hasRole('ADMIN')")
@Validated
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
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

    // 1️⃣ CREATE PRODUCT
    @PostMapping("/createProduct")
    public ResponseEntity<CreatedProductResponseDTO> createProduct(
            @Valid @RequestBody CreateProductRequestDTO dto,
            HttpServletRequest request) {
        // product will be created with isActive=true and isDeleted=false
        CreatedProductResponseDTO response = productService.createProduct(dto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        // Log admin activity
        logActivity(
                response.getId(),
                ProductActivityType.PRODUCT_CREATED,
                "Admin '" + adminEmail + "' created product '"
                        + response.getName()
                        + "' (ASIN: " + response.getAsin() + ").",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2️⃣ UPDATE PRODUCT
    @PutMapping("/updateProduct")
    public ResponseEntity<UpdateProductResponseDTO> updateProduct(
            @Valid @RequestBody UpdateProductRequestDTO dto,
            HttpServletRequest request) {
        // can only update product with flags isActive=true/false and isDeleted=false
        UpdateProductResponseDTO response = productService.updateProduct(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        logActivity(
                response.getProductId(),
                ProductActivityType.PRODUCT_UPDATED,
                "Admin '" + adminEmail + "Updated product '" + response.getName()
                        + "' (ASIN: " + response.getAsin()
                        + "). Updated fields: "
                        + String.join(", ", response.getUpdatedFields()) + ".",
                request
        );
        return ResponseEntity.ok(response);
    }

    // 3️⃣ PUBLISH PRODUCT
    @PutMapping("/publishProduct/{productId}")
    public ResponseEntity<String> publishProduct(@PathVariable("productId") Long productId, HttpServletRequest request) {
        // publish product with flags isActive=true and isDeleted=false
        // atleast one variant and in variants atleast one image and size should be isActive=true and isDeleted=false
        String message = productService.publishProduct(productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        logActivity(
                productId,
                ProductActivityType.PRODUCT_PUBLISHED,
                "Admin '" + adminEmail + "' Published product with ID " + productId + ".",
                request
        );
        return ResponseEntity.ok(message);
    }

    // 3️⃣.1 UNPUBLISH PRODUCT
    @PutMapping("/unpublishProduct/{productId}")
    public ResponseEntity<String> unpublishProduct(@PathVariable("productId") Long productId, HttpServletRequest request) {
        // product should have flags  isPublished=true, isActive=true and isDeleted=false
        String message = productService.unpublishProduct(productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        logActivity(
                productId,
                ProductActivityType.PRODUCT_UNPUBLISHED,
                "Admin '" + adminEmail + "Unpublished product with ID " + productId + ".",
                request
        );        return ResponseEntity.ok(message);
    }

    // 4️⃣ GET PRODUCT BY ID
    @GetMapping("/getProductById/{productId}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable("productId") Long productId) {
        // product should be isActive=true/false and isDeleted=false
        // only will get list of isActive=true/false and isDeleted=false variants/images/sizes
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    // 5️⃣ GET PRODUCT BY ASIN
    @GetMapping("/getProductByAsin/{asin}")
    public ResponseEntity<ProductResponseDTO> getProductByAsin(@PathVariable("asin")  String asin) {
        // product should be isActive=true/false and isDeleted=false
        // only will get list of isActive=true/false and isDeleted=false variants/images/sizes
        return ResponseEntity.ok(productService.getProductByAsin(asin));
    }

    // 6️⃣ GET DELETED PRODUCT BY ID
    @GetMapping("/getDeletedProduct/{productId}")
    public ResponseEntity<ProductResponseDTO> getDeletedProductById(@PathVariable("productId") Long productId) {
        // product should be isActive=false and isDeleted=true
        // only will get list of isActive=false and isDeleted=true variants/images/sizes
        return ResponseEntity.ok(productService.getDeletedProductById(productId));
    }

    // 7️⃣ LIST ALL DELETED PRODUCTS (PAGINATED)
    @GetMapping("/getDeletedProducts")
    public ResponseEntity<Page<DeletedProductListResponseDTO>> getAllDeletedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size); // no sort — ORDER BY is hardcoded in SQL
        // product should be isActive=false and isDeleted=true
        // order will be latest deleted to older deleted
        return ResponseEntity.ok(productService.getAllDeletedProducts(pageable));
    }

    // 8️⃣ LIST ALL NON-DELETED PRODUCTS (PAGINATED)
    @GetMapping("/getAllProducts")
    public ResponseEntity<Page<ProductListResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // will get list of isActive=true/false and isDeleted=false
        // order will be latest created to older created
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // 8️⃣.1️⃣ LIST ALL PUBLISHED PRODUCTS (PAGINATED)
    @GetMapping("/getAllPublishedProducts")
    public ResponseEntity<Page<ProductListResponseDTO>> getAllPublishedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // will get list of isPublish=true, isActive=true and isDeleted=false
        // order will be latest published to older published
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllPublishedProducts(pageable));
    }

    // 8️⃣.2️⃣ LIST ALL READY TO PUBLISHED PRODUCTS (PAGINATED)
    @GetMapping("/getAllReadyToPublishProducts")
    public ResponseEntity<Page<ProductListResponseDTO>> getAllReadyToPublishProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // will get list of isPublish=false,
        // product isActive=true and isDeleted=false
        // alteast one variant isActive=active and isDeleted=false
        // that variant should have alteast one isActive=active and isDeleted=false image and size
        // order will be latest created to older created
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllReadyToPublishProducts(pageable));
    }

    // 9️⃣ DEACTIVATE PRODUCT
    @PutMapping("/deactivateProduct/{productId}")
    public ResponseEntity<String> deactivateProduct(@PathVariable("productId") Long productId, HttpServletRequest request,@RequestBody @Valid String reason) {
        // product should be isActive=true and isDeleted=flase
        String message = productService.deactivateProduct(productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        String cleanReason = reason.replace("\"", "").trim();
        logActivity(
                productId,
                ProductActivityType.PRODUCT_DEACTIVATED,
                "Admin '" + adminEmail + "Deactivated product with ID " + productId + "."+"(Reason: "+ cleanReason + ")",
                request
        );        return ResponseEntity.ok(message);

    }

    // 1️⃣0️⃣ ACTIVATE PRODUCT
    @PutMapping("/activateProduct/{productId}")
    public ResponseEntity<String> activateProduct(@PathVariable("productId") Long productId, HttpServletRequest request) {
        // product should be isActive=false and isDeleted=false
        String message = productService.activateProduct(productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        logActivity(
                productId,
                ProductActivityType.PRODUCT_ACTIVATED,
                "Admin '" + adminEmail +"Activated product with ID " + productId + ".",
                request
        );
        return ResponseEntity.ok(message);
    }

    // 1️⃣1️⃣ DELETE PRODUCT (soft delete)
    @DeleteMapping("/deleteProduct/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long productId, HttpServletRequest request,
                                                @RequestBody @Valid String reason) {
        // product should be isActive=true/false and isDeleted=false
        String response = productService.deleteProduct(productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        Long deletedProductId = productId;
        String cleanReason = reason.replace("\"", "").trim();
        String description = String.format("Admin %s deleted product ID (Target ID: %d) (Reason: %s)", adminEmail, deletedProductId, cleanReason);
        logActivity(
                deletedProductId,
                ProductActivityType.PRODUCT_DELETED,
                description,
                request
        );
        return ResponseEntity.ok(response);
    }

    // 1️⃣2️⃣ RESTORE DELETED PRODUCT
    @PutMapping("/restoreProduct/{productId}")
    public ResponseEntity<String> restoreProduct(@PathVariable("productId") Long productId, HttpServletRequest request) {
        // product should be isActive=true/false and isDeleted=true
        String response=productService.restoreDeletedProduct(productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        logActivity(
                productId,
                ProductActivityType.PRODUCT_RESTORED,
                "Admin '" + adminEmail + "' restored product with ID " + productId + ".",
                request
        );
        return ResponseEntity.ok(response);
    }


}
