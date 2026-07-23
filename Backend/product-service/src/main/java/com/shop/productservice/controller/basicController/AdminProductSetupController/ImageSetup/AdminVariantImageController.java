package com.shop.productservice.controller.basicController.AdminProductSetupController.ImageSetup;

import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.CreateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.UpdateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.CreatedProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.DeletedImageListResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.UpdateProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.DeletedSizeListResponseDTO;
import com.shop.productservice.service.imageService.ProductVariantImageService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/adminProduct/images")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminVariantImageController {

    private final ProductVariantImageService imageService;
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

    // 1️⃣ CREATE IMAGE
    @PostMapping("/createImage")
    public ResponseEntity<CreatedProductVariantImageResponseDTO> createImage(
            @Valid @RequestBody CreateProductVariantImageRequestDTO dto, 
            HttpServletRequest request) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        // image's url and sort order exist check for isActive=true/false and isDeleted=true/false
        // after image creation flags: isActive=true and isDeleted=false
        CreatedProductVariantImageResponseDTO response = imageService.createImage(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        logActivity(
                response.getProductId(),
                ProductActivityType.IMAGE_CREATED,
                "Admin '" + adminEmail
                        + "' created image (Image ID: " + response.getImageId()
                        + ", URL: " + response.getImage()
                        + ") for Variant ID " + response.getVariantId()
                        + " under Product ID " + response.getProductId() + ".",
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2️⃣ UPDATE IMAGE
    @PutMapping("/updateImage")
    public ResponseEntity<UpdateProductVariantImageResponseDTO> updateImage(
            @Valid @RequestBody UpdateProductVariantImageRequestDTO dto,
            HttpServletRequest request) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        //image must exist as isActive=true/false and isDeleted=false
        // image's url and sort order exist check for isActive=true/false and isDeleted=true/false
        UpdateProductVariantImageResponseDTO response = imageService.updateImage(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        logActivity(
                response.getProductId(),
                ProductActivityType.IMAGE_UPDATED,
                "Admin '" + adminEmail
                        + "' updated image (Image ID: " + response.getImageId()
                        + ") for Variant ID " + response.getVariantId()
                        + " under Product ID " + response.getProductId()
                        + ". Updated fields: "
                        + String.join(", ", response.getUpdatedFields()) + ".",
                request
        );
        return ResponseEntity.ok(response);
    }

    // 3️⃣ GET IMAGE BY ID (non-deleted only)
    @GetMapping("/{imageId}")
    public ResponseEntity<ProductVariantImageResponseDTO> getImageById(@PathVariable("imageId") Long imageId) {
        // get only isactive=true/false and isDeleted=false size only
        return ResponseEntity.ok(imageService.getImageById(imageId));
    }

    // 4️⃣ GET ALL IMAGES (non-deleted only, PAGINATED)
    @GetMapping("/allImages")
    public ResponseEntity<Page<ProductVariantImageResponseDTO>> getAllImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // image list will be of isActive=true/false and isDeleted=false images
        // order will be latest created to older created
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(imageService.getAllImages(pageable));
    }

    // 5️⃣ GET IMAGES BY VARIANT ID
    @GetMapping("/imagesUnderVariant/{variantId}")
    public ResponseEntity<List<ProductVariantImageResponseDTO>> getImagesByVariantId(@PathVariable("variantId") Long variantId) {
        // parent varint must be isActive=true/false and isDeleted=false
        // image list will be of isActive=true/false and isDeleted=false images
        // order will be latest created to older created
        return ResponseEntity.ok(imageService.getImagesByVariantId(variantId));
    }

    // 6️⃣ GET DELETED IMAGE BY ID
    @GetMapping("/deletedImage/{imageId}")
    public ResponseEntity<ProductVariantImageResponseDTO> getDeletedImageById(@PathVariable("imageId") Long imageId) {
        // parent product/variant can be active/inactive/deleted
        // image should be isActive=false and isDeleted=true
        return ResponseEntity.ok(imageService.getDeletedImageById(imageId));
    }

    // 7️⃣ GET ALL DELETED IMAGES (PAGINATED)
    @GetMapping("/allDeletedImages")
    public ResponseEntity<Page<DeletedImageListResponseDTO>> getAllDeletedImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // parent product/variant can be active/inactive/deleted
        // image list will be of isActive=false and isDeleted=true images
        // order will be latest deleted to older deleted
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(imageService.getAllDeletedImages(pageable));
    }

    // 8️⃣ DEACTIVATE IMAGE
    @PutMapping("/deactivateImage/{imageId}")
    public ResponseEntity<String> deactivateImage(@PathVariable("imageId") Long imageId, HttpServletRequest request, @RequestBody @Valid String reason) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        // image must be isActive=true and isDeleted=false
        String response = imageService.deactivateImage(imageId,request, reason);
        //activity log in service layer
        return ResponseEntity.ok(response);
    }

    // 9️⃣ ACTIVATE IMAGE
    @PutMapping("/activateImage/{imageId}")
    public ResponseEntity<String> activateImage(@PathVariable("imageId") Long imageId, HttpServletRequest request) {
        // parent product and parent variant should be isActive=true/false and isDeleted=false
        // size must be isActive=false and isDeleted=false
        String response = imageService.activateImage(imageId,request);
        //activity log in service layer
        return ResponseEntity.ok(response);
    }

    // 🔟 DELETE IMAGE (soft delete)
    @DeleteMapping("/deleteImage/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable("imageId") Long imageId, HttpServletRequest request,@RequestBody @Valid String reason) {
        // image should be isActive=true/false and isDeleted=flase
        // parent product/variant should be isActive=true/false and isDeleted=flase
        String response = imageService.deleteImage(imageId,request,reason);
        //activity log in service layer
        return ResponseEntity.ok(response);
    }

    // 1️⃣1️⃣ RESTORE IMAGE
    @PutMapping("/restoreImage/{imageId}")
    public ResponseEntity<String> restoreImage(@PathVariable("imageId") Long imageId, HttpServletRequest request) {
        // image should be isActive=false and isDeleted=true
        // parent product/variant should be isActive=true/false and isDeleted=flase
        // no conflit will be there, because while creation/updation we are checking record's uniquiness for imageurl and sort order
        String response = imageService.restoreImage(imageId,request);
        //activity log in service layer
        return ResponseEntity.ok(response);
    }

    // 1️⃣2️⃣ COUNT IMAGES PER VARIANT
    @GetMapping("/imagesUnderVariant/{variantId}/count")
    public ResponseEntity<Long> countImagesByVariant(@PathVariable("variantId") Long variantId) {
        return ResponseEntity.ok(imageService.countImagesByVariant(variantId));
    }

    // 1️⃣3️⃣ GET ACTIVE IMAGES BY VARIANT FOR ADMIN
    @GetMapping("/activeImages/variant/{variantId}")
    public ResponseEntity<List<ProductVariantImageResponseDTO>>
    getActiveImagesByVariantForAdmin(@PathVariable("variantId") Long variantId) {
        return ResponseEntity.ok(imageService.getActiveImagesByVariantForAdmin(variantId));
    }


    // Get all deleted images under a variant.
    @GetMapping("/variant/{variantId}/deletedImages")
    public ResponseEntity<Page<DeletedImageListResponseDTO>>
    getDeletedImagesByVariantId(@PathVariable("variantId") Long variantId, Pageable pageable) {
        // parent product should be isActive=true/false and isDeleted=true/flase
        // will get list of only deleted images under active/inactive/deleted variant
        return ResponseEntity.ok(
                imageService.getDeletedImagesByVariantId(
                        variantId,
                        pageable
                )
        );
    }

}
