package com.shop.productservice.service.imageService;

import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.CreateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.UpdateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.CreatedProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.DeletedImageListResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.UpdateProductVariantImageResponseDTO;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.enums.ProductActivityType;
import com.shop.productservice.exception.InvalidProductException;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.mapper.ProductVariantImageMapper;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantImageRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.service.activityService.ProductActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ProductVariantImageServiceImpl implements ProductVariantImageService {

    private final ProductVariantImageRepository imageRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantImageMapper imageMapper;
    private final ProductRepository productRepository;
    private final ProductActivityService activityService;


    @Override
    @Transactional
    public CreatedProductVariantImageResponseDTO createImage(CreateProductVariantImageRequestDTO dto) {

        // --------------------- VALIDATION ---------------------
        if (dto.getVariantId() == null || dto.getVariantId() <= 0) {
            throw new InvalidProductException("Invalid variant ID.");
        }

        if (dto.getSortOrder() == null || dto.getSortOrder() < 1) {
            throw new InvalidProductException("Sort order must be >= 1.");
        }

        // --------------------- Validate VARIANT ---------------------
        ProductVariant variant = variantRepository.findById(dto.getVariantId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Variant not found with ID: " + dto.getVariantId()
                ));

        // ---------------------- 1.1 VALIDATE product ----------------------
        Product parentProduct = productRepository.findById(variant.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Product not found with id: " + variant.getProduct().getId() + " OR Either in deleted state"
                ));

        // 3. CONFLICT PROBE (Checks all active/inactive and deleted records via Native Query)
        Optional<ProductVariantImage> exactConflict =
                imageRepository.findExactConflict(
                        variant.getId(),
                        dto.getImage(),
                        dto.getSortOrder());

        if (exactConflict.isPresent()) {
            throw new InvalidProductException(
                    "An image with the same URL and sort order already exists for this variant in "
                            + getImageState(exactConflict.get()) + " state."
            );
        }

        Optional<ProductVariantImage> imageConflict =
                imageRepository.findImageUrlConflict(
                        variant.getId(),
                        dto.getImage());

        if (imageConflict.isPresent()) {

            throw new InvalidProductException(
                    "This image URL already exists for this variant in "
                            + getImageState(imageConflict.get()) + " state."
            );
        }

        Optional<ProductVariantImage> sortConflict =
                imageRepository.findSortOrderConflict(
                        variant.getId(),
                        dto.getSortOrder());

        if (sortConflict.isPresent()) {

            throw new InvalidProductException(
                    "Sort order " + dto.getSortOrder()
                            + " is already assigned to an image in "
                            + getImageState(sortConflict.get()) + " state."
            );
        }

        // --------------------- MAP DTO → ENTITY ---------------------
        ProductVariantImage image = imageMapper.toEntity(dto);
        image.setVariant(variant);

        // --------------------- SAVE ---------------------
        ProductVariantImage saved = imageRepository.save(image);

        // --------------------- MAINTAIN BIDIRECTIONAL LINK ---------------------
        variant.getImages().add(saved);

        // --------------------- RESPONSE ---------------------
        return imageMapper.toCreatedResponseDTO(saved);
    }

    private String getImageState(ProductVariantImage image) {

        if (image.isDeleted()) {
            return "DELETED";
        }

        return image.isActive() ? "ACTIVE" : "INACTIVE";
    }

    @Override
    @Transactional
    public UpdateProductVariantImageResponseDTO updateImage(UpdateProductVariantImageRequestDTO dto) {

        // --------------------- VALIDATION ---------------------
        if (dto.getImageId() == null || dto.getImageId() <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH EXISTING IMAGE (non-deleted) ---------------------
        ProductVariantImage currentImage = imageRepository.findById(dto.getImageId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + dto.getImageId()
                ));

        // ---------------------- 1. VALIDATE VARIANT ----------------------
        ProductVariant parentVariant = variantRepository.findById(currentImage.getVariant().getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Variant not found with id: " + currentImage.getVariant().getId() + " OR Either in deleted state"
                ));

        // ---------------------- 1.1 VALIDATE product ----------------------
        Product parentProduct = productRepository.findById(parentVariant.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Product not found with id: " + parentVariant.getProduct().getId() + " OR Either in deleted state"
                ));

        // ---------------------- DETERMINE VALUES TO VALIDATE ----------------------
        String checkImage = dto.getImage() != null ? dto.getImage().trim() : currentImage.getImage();

        Integer checkSortOrder = dto.getSortOrder() != null ? dto.getSortOrder() : currentImage.getSortOrder();

        // ---------------------- CHECK EXACT CONFLICT ----------------------
        Optional<ProductVariantImage> exactConflict =
                imageRepository.findExactConflictForUpdate(
                        parentVariant.getId(),
                        currentImage.getId(),
                        checkImage,
                        checkSortOrder
                );

        if (exactConflict.isPresent()) {
            throw new InvalidProductException(
                    "An image with the same URL and Sort Order already exists for this variant in "
                            + getImageState(exactConflict.get()) + " state."
            );
        }

        // ---------------------- CHECK IMAGE URL CONFLICT ----------------------
        Optional<ProductVariantImage> imageConflict =
                imageRepository.findImageUrlConflictForUpdate(
                        parentVariant.getId(),
                        currentImage.getId(),
                        checkImage
                );

        if (imageConflict.isPresent()) {
            throw new InvalidProductException(
                    "Image URL already exists for this variant in "
                            + getImageState(imageConflict.get()) + " state."
            );
        }

        // ---------------------- CHECK SORT ORDER CONFLICT ----------------------
        Optional<ProductVariantImage> sortConflict =
                imageRepository.findSortOrderConflictForUpdate(
                        parentVariant.getId(),
                        currentImage.getId(),
                        checkSortOrder
                );

        if (sortConflict.isPresent()) {
            throw new InvalidProductException(
                    "Sort Order " + checkSortOrder
                            + " already exists for this variant in "
                            + getImageState(sortConflict.get()) + " state."
            );
        }

        // ---------------------- UPDATE FIELDS ----------------------
        List<String> updatedFields = new ArrayList<>();

        if (dto.getImage() != null &&
                !dto.getImage().equalsIgnoreCase(currentImage.getImage())) {

            currentImage.setImage(dto.getImage().trim());
            updatedFields.add("image");
        }

        if (dto.getSortOrder() != null &&
                !dto.getSortOrder().equals(currentImage.getSortOrder())) {

            currentImage.setSortOrder(dto.getSortOrder());
            updatedFields.add("sortOrder");
        }

        if (dto.getAltText() != null &&
                !dto.getAltText().equals(currentImage.getAltText())) {

            currentImage.setAltText(dto.getAltText());
            updatedFields.add("altText");
        }

        if (updatedFields.isEmpty()) {
            throw new InvalidProductException("No changes detected.");
        }

         // ---------------------- SAVE ----------------------
        ProductVariantImage saved = imageRepository.save(currentImage);

        // ---------------------- RESPONSE ----------------------
        UpdateProductVariantImageResponseDTO response =
                new UpdateProductVariantImageResponseDTO();

        response.setImageId(saved.getId());
        response.setVariantId(parentVariant.getId());
        response.setProductId(parentProduct.getId());
        response.setImage(saved.getImage());
        response.setUpdatedFields(updatedFields);
        response.setMessage("Image updated successfully.");

        return response;
    }

    @Override
    public ProductVariantImageResponseDTO getImageById(Long imageId) {

        // --------------------- VALIDATION ---------------------
        if (imageId == null || imageId <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH IMAGE (non-deleted only due to @Where) ---------------------
        ProductVariantImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + imageId
                ));

        // --------------------- MAP RESPONSE ---------------------
        return imageMapper.toResponseDTO(image);
    }

    @Override
    public Page<ProductVariantImageResponseDTO> getAllImages(Pageable pageable) {

        // --------------------- FETCH ALL NON-DELETED IMAGES ---------------------
        // @SQLRestriction ensures deleted images are excluded automatically
        Page<ProductVariantImage> images = imageRepository.findAll(PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        ));

        // --------------------- EMPTY LIST → EXCEPTION ---------------------
        if (images == null || images.isEmpty()) {
            throw new ProductNotFoundException("No images found.");
        }

        // --------------------- MAP TO RESPONSE ---------------------
        return images.map(imageMapper::toResponseDTO);
    }

    @Override
    public List<ProductVariantImageResponseDTO> getImagesByVariantId(Long variantId) {

        // --------------------- VALIDATION ---------------------
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID.");
        }

        // --------------------- CHECK VARIANT EXISTS ---------------------
        variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Variant not found with ID: " + variantId
                ));

        // --------------------- FETCH IMAGES (NON-DELETED ONLY) ---------------------
        List<ProductVariantImage> images = imageRepository.findByVariant_IdOrderByCreatedAtDesc(variantId);

        // --------------------- EMPTY LIST → EXCEPTION ---------------------
        if (images == null || images.isEmpty()) {
            throw new ProductNotFoundException("No images found for this variant.");
        }

        // --------------------- MAP TO RESPONSE ---------------------
        return imageMapper.toResponseDTOs(images);
    }

    @Override
    public ProductVariantImageResponseDTO getDeletedImageById(Long imageId) {

        // --------------------- VALIDATION ---------------------
        if (imageId == null || imageId <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH INCLUDING DELETED ---------------------
        ProductVariantImage image = imageRepository.findByIdIncludeDeleted(imageId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + imageId
                ));

        // --------------------- MUST BE DELETED ---------------------
        if (!image.isDeleted()) {
            throw new InvalidProductException(
                    "Image with ID " + imageId + " is not deleted."
            );
        }

        Long variantId = image.getVariant().getId();
        // 2️⃣ Fetch parent variant including deleted
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        // --------------------- MAP TO RESPONSE ---------------------
        return imageMapper.toResponseDTO(image);
    }

    @Override
    public Page<DeletedImageListResponseDTO> getAllDeletedImages(Pageable pageable) {

        // --------------------- FETCH DELETED IMAGES ---------------------
        Page<ProductVariantImage> deletedImages = imageRepository.findAllDeleted(pageable);

        // --------------------- EMPTY LIST → EXCEPTION ---------------------
        if (deletedImages == null || deletedImages.isEmpty()) {
            throw new ProductNotFoundException("No deleted images found.");
        }

        // 3️⃣ Map Product -> DTO (same fields as before, Page.map preserves pagination metadata)

        return deletedImages.map(image -> {

            Long variantId = imageRepository.findVariantIdByImageId(image.getId());
            ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Variant not found"));

            Product product = productRepository.findByIdIncludeDeleted(
                            variant.getProduct().getId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Product not found"));

            return new DeletedImageListResponseDTO(
                    image.getId(),
                    variant.getId(),
                    product.getId(),
                    image.getImage(),
                    image.isActive(),
                    image.isDeleted()
            );
        });
    }

    @Override
    public String deactivateImage(Long imageId, HttpServletRequest request, String reason) {

        // --------------------- VALIDATION ---------------------
        if (imageId == null || imageId <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH NON DELETED ---------------------
        ProductVariantImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + imageId
                ));

        // --------------------- CHECK IF ALREADY InACTIVE ---------------------
        if (!image.isActive()) {
            throw new InvalidProductException(
                    "Image with ID " + imageId + " is already active."
            );
        }

        // ---------------------- 3. CHECK PARENT NOT DELETED ----------------------
        Long variantId = image.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            return "Parent variant is already deleted. Image is implicitly deleted.";
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            return "Parent product is already deleted. Image is implicitly deleted.";
        }

        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();
        Long loggedImageId = image.getId();
        String loggedImageUrl = image.getImage();

        String cleanReason = reason.replace("\"", "").trim();

        // --------------------- UPDATE STATUS ---------------------
        image.setActive(false);
        imageRepository.save(image);

        boolean unpublished = false;
        if (!product.hasSellableVariant()) {
            product.markUnpublished();
            unpublished = true;
        }

        productRepository.save(product);

        logImageActivity(
                loggedProductId,
                loggedVariantId,
                loggedImageId,
                loggedImageUrl,
                ProductActivityType.IMAGE_DEACTIVATED,
                cleanReason,
                request
        );

        // --------------------- RESPONSE ---------------------
        return unpublished
                ? "Image deactivated successfully, As this product has no active selling variants/sizes/images so its is marked as UNPUBLISHED."
                : "Image deactivated successfully.";
    }

    @Override
    public String activateImage(Long imageId, HttpServletRequest request) {

        // --------------------- VALIDATION ---------------------
        if (imageId == null || imageId <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH NON DELETED ---------------------
        ProductVariantImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + imageId
                ));

        // --------------------- CHECK IF ALREADY ACTIVE ---------------------
        if (image.isActive()) {
            throw new InvalidProductException(
                    "Image with ID " + imageId + " is already active."
            );
        }

        // ---------------------- 3. CHECK PARENT NOT DELETED ----------------------
        Long variantId = image.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            return "Cannot activate image because parent variant is deleted.";
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            return "Cannot activate image because parent product is deleted.";
        }

        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();
        Long loggedImageId = image.getId();
        String loggedImageUrl = image.getImage();

        // --------------------- UPDATE STATUS ---------------------
        image.setActive(true);
        imageRepository.save(image);

        logImageActivity(
                loggedProductId,
                loggedVariantId,
                loggedImageId,
                loggedImageUrl,
                ProductActivityType.IMAGE_ACTIVATED,
                null,
                request
        );

        // --------------------- RESPONSE ---------------------
        return "Image activated successfully.";
    }

    @Override
    public String deleteImage(Long imageId, HttpServletRequest request, String reason) {

        // --------------------- VALIDATION ---------------------
        if (imageId == null || imageId <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH INCLUDING DELETED ---------------------
        ProductVariantImage image = imageRepository.findByIdIncludeDeleted(imageId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + imageId
                ));

        // --------------------- ALREADY DELETED? ---------------------
        if (image.isDeleted()) {
            throw new InvalidProductException("Image with ID " + imageId + " is already deleted.");
        }

        // CHECK ON PARENT
        Long variantId = image.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            return "Parent variant is already deleted. Image is implicitly deleted.";
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            return "Parent product is already deleted. Image is implicitly deleted.";
        }

        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();
        Long loggedImageId = image.getId();
        String loggedImageUrl = image.getImage();

        String cleanReason = reason.replace("\"", "").trim();

        // ---------------------- 4. SOFT DELETE ----------------------
        image.setDeleted(true);
        image.setActive(false);

        imageRepository.save(image);

        boolean unpublished = false;
        if (!product.hasSellableVariant()) {
            product.markUnpublished();
            unpublished = true;
        }

        productRepository.save(product);

        logImageActivity(
                loggedProductId,
                loggedVariantId,
                loggedImageId,
                loggedImageUrl,
                ProductActivityType.IMAGE_DELETED,
                cleanReason,
                request
        );

        // --------------------- RESPONSE ---------------------
        return unpublished
                ? "Image deleted successfully, As this product has no active selling variants/sizes/images so its is marked as UNPUBLISHED."
                : "Image deleted successfully.";

    }

    @Override
    @Transactional
    public String restoreImage(Long imageId, HttpServletRequest request) {

        // --------------------- VALIDATION ---------------------
        if (imageId == null || imageId <= 0) {
            throw new InvalidProductException("Invalid image ID.");
        }

        // --------------------- FETCH INCLUDING DELETED ---------------------
        ProductVariantImage image = imageRepository.findByIdIncludeDeleted(imageId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Image not found with ID: " + imageId
                ));

        // --------------------- MUST BE DELETED ---------------------
        if (!image.isDeleted()) {
            throw new InvalidProductException(
                    "Image with ID " + imageId + " is already restore."
            );
        }

        // --------------------- CHECK IF PARENT variant IS DELETED ---------------------
        Long variantId = image.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            throw new InvalidProductException(
                    "Parent variant is already deleted. Image is implicitly deleted."
            );
        }

        // -------------------- 4. Restore only if parent product is NOT deleted ---------------------
        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Parent product not found with ID: " + productId
                        )
                );

        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Parent product is already deleted. Image is implicitly deleted."
            );
        }

        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();
        Long loggedImageId = image.getId();
        String loggedImageUrl = image.getImage();

        // --------------------- RESTORE IMAGE ---------------------
        image.setDeleted(false);
//        image.setActive(false);  // remain inactive until admin activates

        imageRepository.save(image);

        logImageActivity(
                loggedProductId,
                loggedVariantId,
                loggedImageId,
                loggedImageUrl,
                ProductActivityType.IMAGE_RESTORED,
                null,
                request
        );
        return "Image restored successfully.";
    }

    @Override
    public Page<DeletedImageListResponseDTO> getDeletedImagesByVariantId(Long variantId, Pageable pageable){

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID.");
        }

        // ---------------------- 2. VALIDATE VARIANT (INCLUDING DELETED) ----------------------
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Variant not found with ID: " + variantId
                        )
                );

        // ---------------------- 3. FETCH DELETED IMAGES ----------------------
        Page<ProductVariantImage> deletedImages =
                imageRepository.findDeletedImagesByVariantId(
                        variantId,
                        pageable
                );

        // ---------------------- 4. MAP RESPONSE ----------------------
        return deletedImages.map(image ->
                new DeletedImageListResponseDTO(

                        image.getId(),
                        variantId,
                        variant.getProduct().getId(),
                        image.getImage(),
                        image.isActive(),
                        image.isDeleted()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countImagesByVariant(Long variantId) {

        // 1️⃣ Validate input
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Variant ID cannot be null");
        }

        // 2️⃣ Fetch variant
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Variant not found with id: " + variantId
                ));

        // 3️⃣ Validate parent product
        Product product = variant.getProduct();
        if (product == null) {
            throw new InvalidProductException("Variant is not linked to any product.");
        }

        // 4️⃣ Count only images visible to customer
        return variant.getImages().stream()
                .filter(img -> img.isActive() && !img.isDeleted())   //active and not deleted
                .filter(img -> img.getVariant() != null              // defensive check
                        && img.getVariant().isActive()        // image variant is active
                        && !img.getVariant().isDeleted())     // image variant is not deleted
                .filter(img -> {
                    Product p = img.getVariant().getProduct();
                    return p != null && p.isActive()
                            && !p.isDeleted() && p.isPublished();      // product visible
                })
                .count();
    }

    @Override
    public List<ProductVariantImageResponseDTO> getActiveImagesByVariantForAdmin(Long variantId) {

        // --------------------- VALIDATION ---------------------
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID.");
        }

        // --------------------- CHECK VARIANT EXISTS ---------------------
        variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Variant not found with ID: " + variantId
                ));

        // --------------------- FETCH ACTIVE IMAGES ONLY ---------------------
        List<ProductVariantImage> images =
                imageRepository.findByVariantIdAndIsActiveTrueOrderBySortOrderAsc(variantId);

        if (images == null || images.isEmpty()) {
            throw new ProductNotFoundException("No active images found for this variant.");
        }

        return imageMapper.toResponseDTOs(images);

    }

    @Override
    public List<ProductVariantImageResponseDTO> getActiveImagesByVariantForCustomer(Long variantId) {

        // ---------- 1. Validate ----------
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID.");
        }

        // ---------- 2. Fetch variant ----------
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Variant not found with ID: " + variantId
                ));

        // ---------- 3. Customer visibility checks ----------
        // Variant must be active
        if (!variant.isActive()) {
            throw new ProductNotFoundException("Variant is not active.");
        }

        // Product must be visible
        Product product = variant.getProduct();
        if (product == null ||
                product.isDeleted() ||
                !product.isActive() ||
                !product.isPublished()) {
            throw new ProductNotFoundException("Product is not visible to customer.");
        }

        // ---------- 4. Fetch active images ----------
        List<ProductVariantImage> images =
                imageRepository.findByVariantIdAndIsActiveTrueOrderBySortOrderAsc(variantId);

        // ---------- 5. Empty? throw ----------
        if (images == null || images.isEmpty()) {
            throw new ProductNotFoundException("No active images found for this variant.");
        }

        // ---------- 6. Map ----------
        return imageMapper.toResponseDTOs(images);
    }

    /**
     * Logs image-related admin activities.
     * <p>
     * This helper is used for:
     * - Activate Image
     * - Deactivate Image
     * - Delete Image
     * - Restore Image
     */
    private void logImageActivity(
            Long productId,
            Long variantId,
            Long imageId,
            String imageUrl,
            ProductActivityType activityType,
            String reason,
            HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        String adminEmail = auth.getName();

        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user."));

        String description;

        switch (activityType) {

            case IMAGE_DEACTIVATED -> {
                description = String.format(
                        "Admin '%s' deactivated image (Image ID: %d, URL: %s) for Variant ID %d under Product ID %d. Reason: %s",
                        adminEmail,
                        imageId,
                        imageUrl,
                        variantId,
                        productId,
                        reason
                );
            }

            case IMAGE_ACTIVATED -> {
                description = String.format(
                        "Admin '%s' activated image (Image ID: %d, URL: %s) for Variant ID %d under Product ID %d.",
                        adminEmail,
                        imageId,
                        imageUrl,
                        variantId,
                        productId
                );
            }

            case IMAGE_DELETED -> {
                description = String.format(
                        "Admin '%s' deleted image (Image ID: %d, URL: %s) from Variant ID %d under Product ID %d. Reason: %s",
                        adminEmail,
                        imageId,
                        imageUrl,
                        variantId,
                        productId,
                        reason
                );
            }

            case IMAGE_RESTORED -> {
                description = String.format(
                        "Admin '%s' restored image (Image ID: %d, URL: %s) for Variant ID %d under Product ID %d.",
                        adminEmail,
                        imageId,
                        imageUrl,
                        variantId,
                        productId
                );
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported image activity type: " + activityType);
        }

        activityService.logActivity(
                adminEmail,
                adminRole,
                productId,
                activityType,
                description,
                request
        );
    }

}
