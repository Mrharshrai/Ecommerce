package com.shop.productservice.service.variantService;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.DeletedProductListResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.CreateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.UpdateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.*;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.enums.ProductActivityType;
import com.shop.productservice.exception.InvalidProductException;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.mapper.ProductVariantImageMapper;
import com.shop.productservice.mapper.ProductVariantMapper;
import com.shop.productservice.mapper.ProductVariantSizeMapper;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantImageRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.repository.ProductVariantSizeRepository;
import com.shop.productservice.service.activityService.ProductActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService{

    private final ProductVariantRepository variantRepo;
    private final ProductRepository productRepo; // For validation
    private final ProductVariantSizeRepository sizeRepository;
    private final ProductVariantImageRepository imageRepository;
    private final ProductVariantMapper variantMapper;
    private final ProductActivityService activityService;
    private final ProductVariantSizeMapper productVariantSizeMapper;
    private final ProductVariantImageMapper productVariantImageMapper;

    @Override
    public CreatedProductVariantResponseDTO createVariant(CreateProductVariantRequestDTO dto) {

        // 1️⃣ Validate product exists, INCLUDING deleted
        Product product = productRepo.findByIdIncludeDeleted(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + dto.getProductId()
                ));

        if (product.isDeleted()) {
            throw new InvalidProductException("Cannot create variant for a deleted product.");
        }

        // check color record present as active/inactive/deleted
        Optional<ProductVariant> existingVariant =
                variantRepo.findColorConflict(
                        product.getId(),
                        dto.getColor().toUpperCase().trim()
                );

        if (existingVariant.isPresent()) {

            ProductVariant variant = existingVariant.get();

            String state = variant.isDeleted()
                    ? "DELETED"
                    : (variant.isActive() ? "ACTIVE" : "INACTIVE");

            throw new InvalidProductException(
                    "Variant with color '" + dto.getColor()
                            + "' already exists for this product in "
                            + state + " state."
            );
        }

        // 2️⃣ Generate SKU code
        String generatedSku = generateVariantSku(product, dto.getVariantName(), dto.getColor());

        // 4️⃣ Map DTO → Entity
        // ------------------------------
        ProductVariant variant = variantMapper.toEntity(dto);

        variant.setSkuCode(generatedSku);
        variant.setProduct(product);
        variant.setActive(true);
        variant.setDeleted(false);
        variant.setTotalProductVariantQuantity(0);

        // 5️⃣ Save variant
        ProductVariant saved = variantRepo.save(variant);

        // 6️⃣ Return Created Response DTO
        return variantMapper.toCreatedResponseDTO(saved);
    }

    private String generateVariantSku(Product product, String variantName, String color) {

        String productCode = "P" + product.getId(); // P12
        String nameCode = variantName.trim().replaceAll("\\s+", "")
                .substring(0, Math.min(4, variantName.trim().length()))
                .toUpperCase();
        String colorCode = color.trim().replaceAll("\\s+", "")
                .substring(0, Math.min(3, color.length()))
                .toUpperCase(); // Red → RED

        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return productCode + "-" + nameCode + "-" + colorCode + "-" + random;
    }


    @Override
    @Transactional
    public UpdateProductVariantResponseDTO updateVariant(UpdateProductVariantRequestDTO dto) {

        // 1️⃣ Validate input
        if (dto == null || dto.getVariantId() == null) {
            throw new InvalidProductException("Variant ID is required for update.");
        }

        // 2️⃣ Fetch variant (must not be deleted because @Where filters deleted rows automatically)
        ProductVariant variant = variantRepo.findById(dto.getVariantId())
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with id: " + dto.getVariantId())
                );

        // 3️⃣ Parent product validation
        Long productId = variant.getProduct().getId();
        Product product = productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Parent product not found with id: " + productId
                        )
                );

        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot update variant because parent product is deleted."
            );
        }

        List<String> updatedFields = new ArrayList<>();

        // 4️⃣ Update variant name (partial update)
        if (dto.getVariantName() != null && !dto.getVariantName().equals(variant.getVariantName())) {
            variant.setVariantName(dto.getVariantName().trim());
            updatedFields.add("variantName");
        }

        // 5️⃣ Save variant
        variantRepo.save(variant);

        // 6️⃣ Build response DTO
        UpdateProductVariantResponseDTO response = new UpdateProductVariantResponseDTO();
        response.setVariantId(variant.getId());
        response.setProductId(productId);
        response.setSkuCode(variant.getSkuCode());
        response.setVariantName(variant.getVariantName());
        response.setUpdatedFields(updatedFields);

        // check if list is not empty
        if (updatedFields.isEmpty()) {
            response.setMessage("No changes were applied");
        } else {
            response.setMessage("Variant updated successfully");
        }

        // 7️⃣ Return response
        return response;
    }

    @Override
    public ProductVariantResponseDTO getVariantById(Long variantId) {

        // 1️⃣ Validate ID
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Variant ID is required.");
        }

        // 2️⃣ Fetch variant (deleted variants are automatically filtered because of @Where)
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with id: " + variantId)
                );

        Long productId = variant.getProduct().getId();

        Product product=productRepo.findByIdIncludeDeleted(productId).orElseThrow(() ->
                new ProductNotFoundException("Product not found with id: " + productId)
        );
        if (product.isDeleted()) {
            throw new InvalidProductException("Cannot access variant because parent product is deleted. Get data from deleted api calls");
        }

        // 3️⃣ Map and return
        return variantMapper.toResponseDTO(variant);
    }

    @Override
    public ProductVariantResponseDTO getVariantBySkuCode(String skuCode) {

        // 1️⃣ validate input
        if (skuCode == null || skuCode.trim().isEmpty()) {
            throw new InvalidProductException("SKU code is required");
        }
        String normalizedSku = skuCode.trim();

        // 2️⃣ lookup - @Where prevents returning deleted rows
        ProductVariant variant = variantRepo.findBySkuCode(normalizedSku)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with SKU: " + normalizedSku)
                );

        Long productId = variant.getProduct().getId();
        Product product=productRepo.findByIdIncludeDeleted(productId).orElseThrow(() ->
                new ProductNotFoundException("Product not found with id: " + productId)
        );

        if (product.isDeleted()) {
            throw new InvalidProductException("Cannot access variant because parent product is deleted. Get data from deleted api calls");
        }

        // 3️⃣ map & return full response (sizes + images included)
        return variantMapper.toResponseDTO(variant);
    }

    @Override
    public Page<ProductVariantResponseDTO> getAllVariants(Pageable pageable) {

        // 1️⃣ Fetch all non-deleted variants as a page
        Page<ProductVariant> variants = variantRepo.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        // 2️⃣ Return if page is empty
        if (variants == null || variants.isEmpty()) {
            throw new ProductNotFoundException("No variant found.");
        }

        // 3️⃣ Map to full response DTOs preserving pagination info
        return variants.map(variantMapper::toResponseDTO);

    }

    @Override
    public List<ProductVariantResponseDTO> getVariantsByProductId(Long productId) {

        // 1️⃣ Validate
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Ensure product exists (admin can access inactive products also)
        productRepo.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Parent product not found with ID: " + productId)
                );

        // 3️⃣ Fetch variants (deleted auto-filtered)
        List<ProductVariant> variants = variantRepo.findByProductIdOrderByCreatedAtDesc(productId);

        // 4️⃣ If none found → return empty list
        if (variants == null || variants.isEmpty()) {
            throw new ProductNotFoundException(
                    "No variant found for variant with id: " + productId
            );
        }

        // 5️⃣ Map to DTO and return
        return variantMapper.toResponseDTOs(variants);
    }


    @Override
    public ProductVariantResponseDTO getDeletedVariantById(Long variantId) {

        // 1️⃣ Validate ID
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID");
        }

        // 2️⃣ Fetch variant including deleted
        ProductVariant variant = variantRepo.findByIdIncludeDeleted(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        // 3️⃣ Ensure it IS deleted
        if (!variant.isDeleted()) {
            throw new InvalidProductException(
                    "Variant with ID " + variantId + " is not deleted"
            );
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        List<ProductVariantSizeResponseDTO> sizeDTOs =
                sizeRepository.findByVariantIdIncludeDeleted(variant.getId())
                        .stream()
                        .filter(ProductVariantSize::isDeleted)
                        .map(productVariantSizeMapper::toResponseDTO)
                        .toList();

        List<ProductVariantImageResponseDTO> imageDTOs =
                imageRepository.findByVariantIdIncludeDeleted(variant.getId())
                        .stream()
                        .filter(ProductVariantImage::isDeleted)
                        .map(productVariantImageMapper::toResponseDTO)
                        .toList();

        // 6️⃣ Map variant
        ProductVariantResponseDTO response = variantMapper.toResponseDTO(variant);

        // 7️⃣ Replace child DTOs
        response.setSizes(sizeDTOs);
        response.setImages(imageDTOs);

        // 8️⃣ Return
        return response;
    }

    @Override
    public Page<DeletedVariantListResponseDTO> getDeletedVariants(Pageable pageable) {

        // 1️⃣ Fetch all deleted variants
        Page<ProductVariant> deletedVariants = variantRepo.findAllDeleted(pageable);

        // 2️⃣ If none, return empty list (not null)
        if (deletedVariants == null || deletedVariants.isEmpty()) {
            throw new ProductNotFoundException("No deleted variants found.");
        }

        // 3️⃣ Map Product -> DTO (same fields as before, Page.map preserves pagination metadata)
        return deletedVariants.map(variant -> new DeletedVariantListResponseDTO(

                variant.getId(),
                variant.getProduct().getId(),
                variant.getSkuCode(),
                variant.getVariantName(),
                variant.getColor(),
                variant.isActive(),
                variant.isDeleted()
        ));
    }

    @Override
    @Transactional
    public String deactivateVariant(Long variantId, String reason, HttpServletRequest request) {

        // 1️⃣ Validate input
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID");
        }

        // 2️⃣ Fetch active (non-deleted) variant
        // NOTE: Deleted variants are filtered by @Where, so they will not be returned.
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        // 5️⃣ If already inactive
        if (!variant.isActive()) {
            return "Variant is already inactive.";
        }

        // 3️⃣ Fetch parent product INCLUDING deleted
        Long productId = variant.getProduct().getId();
        Product product = productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Parent product not found with ID: " + productId
                        )
                );

        // 4️⃣ If product is deleted → no action allowed
        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot deactivate variant because the parent product is deleted."
            );
        }

        // 6️⃣ Deactivate variant and  Save changes
        variant.setActive(false);
        variantRepo.save(variant);

        // 7️⃣ FORCE product total recalculation
        product.recalculateTotalProductQuantity();

        // 8️⃣ Business rule: auto-unpublish if not sellable
        boolean unpublished = false;
        if (!product.hasSellableVariant()) {
            product.markUnpublished();
            unpublished = true;
        }

        productRepo.save(product);

        // 9️⃣ Log admin activity
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user.")
                );

        // Remove extra quotes if request body is a JSON string like "Out of stock"
        String cleanReason = reason.replace("\"", "").trim();
        String description = String.format(
                "Admin '%s' deactivated variant '%s' (Variant ID: %d, SKU: %s) for Product ID %d. Reason: %s",
                adminEmail,
                variant.getVariantName(),
                variant.getId(),
                variant.getSkuCode(),
                product.getId(),
                cleanReason
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                product.getId(),
                ProductActivityType.VARIANT_DEACTIVATED,
                description,
                request
        );

        return unpublished
                ?"Variant deactivated successfully, As this product has no active selling variants/sizes/images so its is marked as UNPUBLISHED."
                :"Variant deactivated successfully.";
    }

    @Override
    @Transactional
    public String activateVariant(Long variantId,HttpServletRequest request) {

        // 1️⃣ Validate input
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID");
        }

        // 2️⃣ Fetch variant (deleted variants are auto-filtered by @Where)
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        // 3️⃣ Check if already active
        if (variant.isActive()) {
            return "Variant is already active.";
        }

        // Check if parent not deleted
        Long productId = variant.getProduct().getId();
        Product product = productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Parent product not found with ID: " + productId
                        )
                );

        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot activate variant because parent product is deleted."
            );
        }

        // 4️⃣ Activate
        variant.setActive(true);
        // 5️⃣ Save
        variantRepo.save(variant);

        // 2️⃣ FORCE product total recalculation
        product.recalculateTotalProductQuantity();
        productRepo.save(product);

        // 9️⃣ Log admin activity
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String adminEmail = auth.getName();

        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user.")
                );

        String description = String.format(
                "Admin '%s' activated variant '%s' (Variant ID: %d, SKU: %s) for Product ID %d.",
                adminEmail,
                variant.getVariantName(),
                variant.getId(),
                variant.getSkuCode(),
                product.getId()
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                product.getId(),
                ProductActivityType.VARIANT_ACTIVATED,
                description,
                request
        );

        return "Variant activated successfully.";
    }

    @Override
    @Transactional
    public String deleteVariant(Long variantId,HttpServletRequest request,String reason) {

        // 1️⃣ Validate input
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID");
        }

        // 2️⃣ Fetch variant including deleted (bypass @Where)
        ProductVariant variant = variantRepo.findByIdIncludeDeleted(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        // 3️⃣ Already deleted?
        if (variant.isDeleted()) {
            return "Variant is already deleted.";
        }

        // Check if parent not deleted
        Long productId = variant.getProduct().getId();
        Product product = productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Parent product not found with ID: " + productId
                        )
                );

        if (product.isDeleted()) {
            return "Parent product is already deleted. Variant is implicitly deleted.";
        }

        // ------------------------------------------------------------------
        // Store values required for activity log BEFORE changing the entity.
        // This prevents any issues after soft delete or entity state changes.
        // ------------------------------------------------------------------
        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();
        String loggedVariantName = variant.getVariantName();
        String loggedSkuCode = variant.getSkuCode();

        // 4️⃣ Soft delete variant + deactivate
        variant.setDeleted(true);
        variant.setActive(false);

        // 5️⃣ delete all sizes
        if (variant.getSizes() != null) {
            for (ProductVariantSize size : variant.getSizes()) {
                size.setActive(false);
                size.setDeleted(true);
            }
        }

        // 6️⃣ delete all images
        if (variant.getImages() != null) {
            for (ProductVariantImage image : variant.getImages()) {
                image.setActive(false);
                image.setDeleted(true);
            }
        }

        // 7️⃣ Save variant (cascade updates children)
        variantRepo.save(variant);

        // 2️⃣ FORCE product total recalculation
        product.recalculateTotalProductQuantity();

        // 3️⃣ Business rule: auto-unpublish if not sellable
        boolean unpublished = false;
        if (!product.hasSellableVariant()) {
            product.markUnpublished();
            unpublished = true;
        }

        productRepo.save(product);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user.")
                );

        String cleanReason = reason.replace("\"", "").trim();
        String description = String.format(
                "Admin '%s' deleted variant '%s' (Variant ID: %d, SKU: %s) from Product ID %d. Reason: %s",
                adminEmail,
                loggedVariantName,
                loggedVariantId,
                loggedSkuCode,
                loggedProductId,
                cleanReason
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                loggedProductId,
                ProductActivityType.VARIANT_DELETED,
                description,
                request
        );

        // 8️⃣ Return success message
        return unpublished
                ? "Variant deleted successfully, As this product has no active selling variants/sizes/images so its is marked as UNPUBLISHED."
                :"Variant deleted successfully (soft delete), can be restored later.";
    }

    @Override
    @Transactional
    public String restoreVariant(Long variantId,HttpServletRequest request) {

        // 1️⃣ Validate input
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Invalid variant ID.");
        }

        // 2️⃣ Fetch variant including deleted (bypass @Where)
        ProductVariant variant = variantRepo.findByIdIncludeDeleted(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        // 3️⃣ Already restored?
        if (!variant.isDeleted()) {
            return "Variant is already restored successfully.";
        }

        // Check if parent not deleted
        Long productId = variant.getProduct().getId();
        Product product = productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Parent product not found with ID: " + productId
                        )
                );

        // 4️⃣ Restore only if parent product is NOT deleted
        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot restore variant because the parent product is deleted."
            );
        }

        // 5️⃣ SKU conflict check — IMPORTANT----THIS WILL NEVER BE THE CASE
        // Check if ANY active (non-deleted) variant already has this SKU
//        if (variantRepo.existsBySkuCode(variant.getSkuCode())) {
//            throw new InvalidProductException(
//                    "Cannot restore variant. Another active variant already exists with SKU: "
//                            + variant.getSkuCode()
//            );
//        }

        // 6️⃣ COLOR CONFLICT CHECK (MANDATORY)
        //never be the case, because in creation itself checking for active/inactive/delted colour records
//        boolean colorExists = variantRepo
//                .existsByProductIdAndColorIgnoreCaseAndIsDeletedFalse(
//                        product.getId(),
//                        variant.getColor().trim()
//                );
//
//        if (colorExists) {
//            throw new InvalidProductException(
//                    "Cannot restore variant. Another active/inactive variant already exists with color: "
//                            + variant.getColor()
//            );
//        }

        // ------------------------------------------------------------------
        // Store values required for activity log before modifying the entity.
        // ------------------------------------------------------------------
        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();
        String loggedVariantName = variant.getVariantName();
        String loggedSkuCode = variant.getSkuCode();

        // 6️⃣ Restore variant (undelete but keep inactive)
        variant.setDeleted(false);   // undelete
//        variant.setActive(false);    // admin will activate manually

        // 7️⃣ Children (sizes & images) are NOT auto-restored
        // Their isActive remains false. Admin will selectively activate.

        // 8️⃣ Save
        variantRepo.save(variant);
        // ------------------------------------------------------------------
        // Log admin activity
        // ------------------------------------------------------------------
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user.")
                );

        String description = String.format(
                "Admin '%s' restored variant '%s' (Variant ID: %d, SKU: %s) for Product ID %d.",
                adminEmail,
                loggedVariantName,
                loggedVariantId,
                loggedSkuCode,
                loggedProductId
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                loggedProductId,
                ProductActivityType.VARIANT_RESTORED,
                description,
                request
        );

        return "Variant restored successfully. Activate it before publishing.";
    }

    /**
     * =============================================================
     * Get all deleted variants belonging to a specific product.
     *
     * Returns only deleted variants.
     * Parent product may be Active or Inactive.
     * Deleted product is not allowed.
     * Ordered by latest deleted first.
     * =============================================================
     */
    @Override
    public  Page<DeletedVariantListResponseDTO> getDeletedVariantsByProductId(
            Long productId,
            Pageable pageable
    ){
        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // ---------------------- 2. VALIDATE PRODUCT (INCLUDING DELETED) ----------------------
        productRepo.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with ID: " + productId
                        )
                );

        // ---------------------- 3. FETCH DELETED VARIANTS ----------------------
        Page<ProductVariant> deletedVariants =
                variantRepo.findDeletedVariantsByProductId(
                        productId,
                        pageable
                );

        // ---------------------- 4. EMPTY RESULT ----------------------
//        if (deletedVariants.isEmpty()) {
//            throw new ProductNotFoundException(
//                    "No deleted variants found for Product ID: " + productId
//            );
//        }

        // ---------------------- 5. MAP RESPONSE ----------------------
        return deletedVariants.map(variant ->
                new DeletedVariantListResponseDTO(

                        variant.getId(),
                        productId,
                        variant.getSkuCode(),
                        variant.getVariantName(),
                        variant.getColor(),
                        variant.isActive(),
                        variant.isDeleted()
                )
        );
    }

    @Override
    public ProductVariantResponseDTO getActiveVariantBySkuCode(String skuCode) {

        // 1️⃣ Validate input
        if (skuCode == null || skuCode.trim().isEmpty()) {
            throw new InvalidProductException("SKU code is required.");
        }

        String normalizedSku = skuCode.trim();

        // 2️⃣ Fetch variant (deleted variants are auto-filtered)
        ProductVariant variant = variantRepo.findBySkuCode(normalizedSku)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with SKU: " + normalizedSku)
                );

        // 3️⃣ Variant must be ACTIVE
        if (!variant.isActive()) {
            throw new ProductNotFoundException(
                    "Variant is inactive or not available for customers."
            );
        }

        // 4️⃣ Parent product must be active + published + not deleted
        Product product = variant.getProduct();

        if (product == null || product.isDeleted()) {
            throw new ProductNotFoundException("Product for this variant does not exist anymore.");
        }

        if (!product.isActive()) {
            throw new ProductNotFoundException("Product is inactive.");
        }

        if (!product.isPublished()) {
            throw new ProductNotFoundException("Product is not published.");
        }

        // 5️⃣ Return mapped full response
        return variantMapper.toResponseDTO(variant);
    }

    @Override
    public List<ProductVariantListResponseDTO> searchVariantsByName(String name) {

        // 1️⃣ Validate
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductException("Search name is required.");
        }

        String normalized = name.trim();

        // 2️⃣ Fetch variants (already filters deleted ones)
        List<ProductVariant> variants = variantRepo.findByVariantNameContainingIgnoreCase(normalized);

        // 3️⃣ Filter customer-visible variants
        List<ProductVariant> visible = variants.stream()
                .filter(ProductVariant::isActive)
                .filter(v -> v.getProduct() != null)
                .filter(v -> v.getProduct().isActive())
                .filter(v -> v.getProduct().isPublished())
                .toList();

        // 4️⃣ Map to list DTO (startingPrice + primaryImage included)
        return variantMapper.toListDTOs(visible);
    }

    @Override
    public List<ProductVariantListResponseDTO> searchVariantsByColor(String color) {

        // 1️⃣ Validate
        if (color == null || color.trim().isEmpty()) {
            throw new InvalidProductException("Color is required.");
        }

        String normalized = color.trim();

        // 2️⃣ Fetch variants (auto-excludes deleted)
        List<ProductVariant> variants = variantRepo.findByColorIgnoreCase(normalized);

        // 3️⃣ Filter only visible to customer
        List<ProductVariant> visible = variants.stream()
                .filter(ProductVariant::isActive)
                .filter(v -> v.getProduct() != null && v.getProduct().isActive())
                .filter(v -> v.getProduct().isPublished())
                .toList();

        // 4️⃣ Map to list DTO (list item response)
        return variantMapper.toListDTOs(visible);
    }

    @Override
    public long countVariantsByProductId(Long productId) {

        // 1️⃣ Validate
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch ALL variants of this product (deleted auto-filtered)
        List<ProductVariant> variants = variantRepo.findByProductId(productId);

        // 3️⃣ Count only variants visible to customer
        return variants.stream()
                .filter(ProductVariant::isActive)
                .filter(v -> v.getProduct() != null && v.getProduct().isActive())
                .filter(v -> v.getProduct().isPublished())
                .count();
    }

}
