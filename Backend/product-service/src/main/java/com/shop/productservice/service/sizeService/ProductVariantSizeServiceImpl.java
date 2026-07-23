package com.shop.productservice.service.sizeService;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.DeletedImageListResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.CreateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.UpdateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.*;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.enums.ProductActivityType;
import com.shop.productservice.enums.Size;
import com.shop.productservice.exception.InvalidProductException;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.mapper.ProductVariantSizeMapper;
import com.shop.productservice.repository.ProductRepository;
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

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ProductVariantSizeServiceImpl implements ProductVariantSizeService{

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository; // For validation
    private final ProductVariantSizeRepository sizeRepository;
    private final ProductVariantSizeMapper sizeMapper;
    private final ProductActivityService  activityService;

    @Override
    public CreatedProductVariantSizeResponseDTO createSize(CreateProductVariantSizeRequestDTO dto) {

        // ---------------------- 1. VALIDATE VARIANT ----------------------
        ProductVariant variant = variantRepository.findById(dto.getVariantId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Variant not found with id: " + dto.getVariantId() + " OR Either in deleted state"
                ));

        // ---------------------- 1.1 VALIDATE product ----------------------
        Product parentProduct = productRepository.findById(variant.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Product not found with id: " + variant.getProduct().getId() + " OR Either in deleted state"
                ));

        // ---------------------- 2. CHECK DUPLICATE SIZE(check all active/inactive/deleted) ----------------------
        Optional<ProductVariantSize> existingSize =
                sizeRepository.findSizeConflict(
                        variant.getId(),
                        dto.getSize().name()
                );

        if (existingSize.isPresent()) {

            ProductVariantSize size = existingSize.get();

            String state = size.isDeleted()
                    ? "DELETED"
                    : (size.isActive() ? "ACTIVE" : "INACTIVE");

            throw new InvalidProductException(
                    "Size (" + dto.getSize() + ") already exists for this variant in "
                            + state + " state."
            );
        }

        // ---------------------- 3. GENERATE SIZE SKU ----------------------
        String productSku = variant.getProduct().getAsin();         // e.g. TSHIRT001
        String color = variant.getColor().toUpperCase();           // e.g. RED
        String sizeCode = dto.getSize().name();                    // e.g. XL

        String generatedSku = productSku + "-" + color + "-" + sizeCode;

        // ---------------------- 5. CHECK SKU UNIQUENESS ---------------------- never be the case
//        if (sizeRepository.existsBySizeSku(generatedSku)) {
//            throw new InvalidProductException(
//                    "Size SKU already exists: " + generatedSku
//            );
//        }

        // ---------------------- 4. DTO -> ENTITY ----------------------
        ProductVariantSize sizeEntity = sizeMapper.toEntity(dto);
        sizeEntity.setVariant(variant);
        sizeEntity.setSizeSku(generatedSku);

        // ---------------------- 6. SAVE SIZE ----------------------
        ProductVariantSize saved = sizeRepository.save(sizeEntity);

        // maintain variant → sizes link
        variant.getSizes().add(saved);

        // ---- FIX: Recalculate quantities for variant and product ----
        variant.recalculateTotalProductVariantQuantity();
        variantRepository.save(variant);

        Product product = variant.getProduct();
        product.recalculateTotalProductQuantity();
        productRepository.save(product);

        // ---------------------- 7. RETURN RESPONSE ----------------------
        return sizeMapper.toCreatedResponseDTO(saved);
    }

    @Override
    @Transactional
    public UpdateProductVariantSizeResponseDTO updateSize(UpdateProductVariantSizeRequestDTO dto) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (dto == null || dto.getSizeId() == null) {
            throw new InvalidProductException("Request cannot be null");
        }

        // ---------------------- 2. FETCH SIZE (NON-DELETED ONLY DUE TO @Where) ----------------------
        ProductVariantSize size = sizeRepository.findById(dto.getSizeId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + dto.getSizeId()
                ));

        // ---------------------- 1. VALIDATE VARIANT ----------------------
        ProductVariant parentVariant = variantRepository.findById(size.getVariant().getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Variant not found with id: " + size.getVariant().getId() + " OR Either in deleted state"
                ));

        // ---------------------- 1.1 VALIDATE product ----------------------
        Product parentProduct = productRepository.findById(parentVariant.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Parent Product not found with id: " + parentVariant.getProduct().getId() + " OR Either in deleted state"
                ));

        // ---------------------- 3. TRACK UPDATED FIELDS ----------------------
        List<String> updatedFields = new ArrayList<>();

        if (dto.getQuantity() != null && !dto.getQuantity().equals(size.getQuantity())) {
            size.setQuantity(dto.getQuantity());
            updatedFields.add("quantity");
        }

        if (dto.getMrp() != null && !dto.getMrp().equals(size.getMrp())) {
            size.setMrp(dto.getMrp());
            updatedFields.add("mrp");
        }

        if (dto.getWeight() != null && !dto.getWeight().equals(size.getWeight())) {
            size.setWeight(dto.getWeight());
            updatedFields.add("weight");
        }

        if (dto.getLength() != null && !dto.getLength().equals(size.getLength())) {
            size.setLength(dto.getLength());
            updatedFields.add("length");
        }

        if (dto.getWidth() != null && !dto.getWidth().equals(size.getWidth())) {
            size.setWidth(dto.getWidth());
            updatedFields.add("width");
        }

        if (dto.getHeight() != null && !dto.getHeight().equals(size.getHeight())) {
            size.setHeight(dto.getHeight());
            updatedFields.add("height");
        }

        if (updatedFields.isEmpty()) {
            throw new InvalidProductException("No fields were updated. Provide at least one value.");
        }

        // ---------------------- 4. SAVE UPDATED SIZE ----------------------
        ProductVariantSize saved = sizeRepository.save(size);

        // ---------------------- 5. RECALCULATE QUANTITIES (ONLY IF QUANTITY UPDATED) ----------------------
        if (updatedFields.contains("quantity")) {

            ProductVariant variant = saved.getVariant();
//            Trigger @PreUpdate in variant
//            variant.recalculateTotalProductVariantQuantity();
            variantRepository.save(variant);

            Product product = variant.getProduct();

//            product.recalculateTotalProductQuantity();
            productRepository.save(product);
        }

        // ---------------------- 5. RETURN RESPONSE ----------------------
        return UpdateProductVariantSizeResponseDTO.builder()
                .sizeId(saved.getId())
                .variantId(saved.getVariant().getId())
                .productId(saved.getVariant().getProduct().getId())
                .size(saved.getSize())
                .updatedFields(updatedFields)
                .message("Size updated successfully.")
                .build();
    }

    @Override
    public ProductVariantSizeResponseDTO getSizeById(Long sizeId) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeId == null || sizeId <= 0) {
            throw new InvalidProductException("Size ID cannot be null");
        }

        // ---------------------- 2. FETCH NON-DELETED SIZE ----------------------
        ProductVariantSize size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + sizeId
                ));

        // ---------------------- 3. MAP TO DTO ----------------------
        return sizeMapper.toResponseDTO(size);
    }

    @Override
    public ProductVariantSizeResponseDTO getSizeBySku(String sizeSku) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeSku == null || sizeSku.trim().isEmpty()) {
            throw new InvalidProductException("Size SKU cannot be null or empty");
        }

        // ---------------------- 2. FETCH NON-DELETED SIZE BY SKU ----------------------
        ProductVariantSize size = sizeRepository.findBySizeSku(sizeSku.toUpperCase().trim())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with SKU: " + sizeSku
                ));

        // ---------------------- 3. MAP TO DTO ----------------------
        return sizeMapper.toResponseDTO(size);
    }

    @Override
    public ProductVariantSizeResponseDTO getDeletedSize(Long sizeId) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeId == null) {
            throw new InvalidProductException("Size ID cannot be null");
        }

        // ---------------------- 2. GET SIZE INCLUDING DELETED ----------------------
        ProductVariantSize size = sizeRepository.findByIdIncludeDeleted(sizeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + sizeId
                ));

        // ---------------------- 3. CHECK IF IT IS ACTUALLY DELETED ----------------------
        if (!size.isDeleted()) {
            throw new InvalidProductException(
                    "Size with id " + sizeId + " is not deleted"
            );
        }

        Long variantId = size.getVariant().getId();
        // 2️⃣ Fetch parent variant including deleted
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Variant not found with ID: " + variantId)
                );

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        // ---------------------- 4. MAP TO RESPONSE DTO ----------------------
        return sizeMapper.toResponseDTO(size);
    }

    @Override
    public Page<DeletedSizeListResponseDTO> getAllDeletedSizes(Pageable pageable) {

        // ---------------------- 1. FETCH ALL DELETED SIZES ----------------------
        Page<ProductVariantSize> deletedSizes = sizeRepository.findAllDeleted(pageable);

        // ---------------------- 2. IF NONE FOUND ----------------------
        if (deletedSizes == null || deletedSizes.isEmpty()) {
            throw new ProductNotFoundException("No deleted sizes found.");
        }

        // ---------------------- 3. MAP TO RESPONSE DTO LIST ----------------------
        return deletedSizes.map(size -> {

            Long variantId = sizeRepository.findVariantIdBySizeId(size.getId());

            ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Variant not found"));

            Product product = productRepository.findByIdIncludeDeleted(
                            variant.getProduct().getId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Product not found"));

            return new DeletedSizeListResponseDTO(
                    size.getId(),
                    variant.getId(),
                    product.getId(),
                    size.getSizeSku(),
                    size.getSize(),
                    size.isActive(),
                    size.isDeleted()
            );
        });
    }

    @Override
    public Page<ProductVariantSizeResponseDTO> getAllSizes(Pageable pageable) {

        // ---------------------- 1. FETCH ALL NON-DELETED SIZES ----------------------
        Page<ProductVariantSize> sizes = sizeRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        // ---------------------- 2. IF EMPTY ----------------------
        if (sizes == null || sizes.isEmpty()) {
            throw new ProductNotFoundException("No sizes found.");
        }

        // ---------------------- 3. MAP AND RETURN ----------------------
        return sizes.map(sizeMapper::toResponseDTO);
    }

    @Override
    public List<ProductVariantSizeResponseDTO> getSizesByVariantId(Long variantId) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (variantId == null || variantId <= 0) {
            throw new InvalidProductException("Variant ID cannot be null");
        }

        // ---------------------- 2. VALIDATE VARIANT EXISTS ----------------------
        if (!variantRepository.existsById(variantId)) {
            throw new ProductNotFoundException("Variant not found with id: " + variantId);
        }

        // ---------------------- 3. FETCH SIZES FOR VARIANT ----------------------
        List<ProductVariantSize> sizes = sizeRepository.findByVariant_IdOrderByCreatedAtDesc(variantId);

        // ---------------------- 4. IF NONE FOUND ----------------------
        if (sizes == null || sizes.isEmpty()) {
            throw new ProductNotFoundException(
                    "No sizes found for variant with id: " + variantId
            );
        }

        // ---------------------- 5. MAP AND RETURN ----------------------
        return sizeMapper.toResponseDTOs(sizes);
    }

    @Override
    @Transactional
    public String deactivateSize(Long sizeId, HttpServletRequest request, String reason) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeId == null || sizeId <= 0) {
            throw new InvalidProductException("Size ID cannot be null");
        }

        // ---------------------- 2. FETCH SIZE (NON-DELETED ONLY) ----------------------
        ProductVariantSize size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + sizeId
                ));

        // ---------------------- 3. CHECK IF ALREADY DEACTIVATED ----------------------
        if (!size.isActive()) {
            return
                    "Size with id " + sizeId + " is already deactivated.";
        }

        // ---------------------- 3. CHECK PARENT NOT DELETED ----------------------
        Long variantId = size.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            throw new InvalidProductException(
                    "Parent Variant is already deleted, No functional changes required."
            );
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Parent Product is already deleted, No functional changes required."
            );
        }

        // Store values required for activity log BEFORE changing entity.
        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();

        Long loggedSizeId = size.getId();
        String loggedSizeSku = size.getSizeSku();
        Size loggedSize = size.getSize();

        // ---------------------- 4. DEACTIVATE ----------------------
        size.setActive(false); // or size.setIsActive(false);
        sizeRepository.save(size);

        // 2️⃣ FORCE product and variant total recalculation
        variant.recalculateTotalProductVariantQuantity();
        variantRepository.save(variant);

        product.recalculateTotalProductQuantity();

        // 3️⃣ Business rule: auto-unpublish if not sellable
        boolean unpublished = false;
        if (!product.hasSellableVariant()) {
            product.markUnpublished();
            unpublished = true;
        }

        productRepository.save(product);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String adminEmail = auth.getName();

        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user.")
                );

        // Remove surrounding quotes if JSON string body
        String cleanReason = reason.replace("\"", "").trim();

        String description = String.format(
                "Admin '%s' deactivated size '%s' (Size ID: %d, Size SKU: %s) for Variant ID %d under Product ID %d. Reason: %s",
                adminEmail,
                loggedSize,
                loggedSizeId,
                loggedSizeSku,
                loggedVariantId,
                loggedProductId,
                cleanReason
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                loggedProductId,
                ProductActivityType.SIZE_DEACTIVATED,
                description,
                request
        );

        // ---------------------- 5. RETURN MESSAGE ----------------------
        return unpublished
                ?"Size deactivated successfully, As this product has no active selling variants/sizes/images so its is marked as UNPUBLISHED."
                :"Size with ID " + sizeId + " has been deactivated successfully.";
    }

    @Override
    @Transactional
    public String activateSize(Long sizeId,HttpServletRequest request) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeId == null || sizeId <= 0) {
            throw new InvalidProductException("Size ID cannot be null");
        }

        // ---------------------- 2. FETCH SIZE (NON-DELETED ONLY) ----------------------
        ProductVariantSize size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + sizeId
                ));

        // ---------------------- 3. CHECK IF ALREADY ACTIVE ----------------------
        if (size.isActive()) {
            throw new InvalidProductException(
                    "Size with id " + sizeId + " is already active."
            );
        }

        // Check if parents not deleted
        Long variantId = size.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot activate size because parent variant is deleted."
            );
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot activate size because parent product is deleted."
            );
        }

        // Store values required for activity log BEFORE changing entity.
        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();

        Long loggedSizeId = size.getId();
        String loggedSizeSku = size.getSizeSku();
        Size loggedSize = size.getSize();

        // ---------------------- 4. ACTIVATE ----------------------
        size.setActive(true);

        sizeRepository.save(size);

        // 2️⃣ FORCE product and variant total recalculation
        variant.recalculateTotalProductVariantQuantity();
        variantRepository.save(variant);

        product.recalculateTotalProductQuantity();
        productRepository.save(product);

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
                "Admin '%s' activated size '%s' (Size ID: %d, Size SKU: %s) for Variant ID %d under Product ID %d.",
                adminEmail,
                loggedSize,
                loggedSizeId,
                loggedSizeSku,
                loggedVariantId,
                loggedProductId
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                loggedProductId,
                ProductActivityType.SIZE_ACTIVATED,
                description,
                request
        );

        // ---------------------- 5. RETURN MESSAGE ----------------------
        return "Size with ID " + sizeId + " has been activated successfully.";
    }

    @Override
    @Transactional
    public String deleteSize(Long sizeId, HttpServletRequest request, String reason) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeId == null || sizeId <= 0) {
            throw new InvalidProductException("Size ID cannot be null");
        }

        // ---------------------- 2. FETCH SIZE (NON-DELETED ONLY) ----------------------
        ProductVariantSize size = sizeRepository.findByIdIncludeDeleted(sizeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + sizeId
                ));

        // ---------------------- 3. CHECK IF ALREADY DELETED ----------------------
        if (size.isDeleted()) {
            throw new InvalidProductException(
                    "Size with id " + sizeId + " is already deleted."
            );
        }

        Long variantId = size.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            return "Parent variant is already deleted. Size is implicitly deleted.";
        }

        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            return "Parent product is already deleted. Size is implicitly deleted.";
        }

        // Store values required for activity log BEFORE modifying entity.
        // This avoids any issues after soft delete.
        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();

        Long loggedSizeId = size.getId();
        String loggedSizeSku = size.getSizeSku();
        Size loggedSize = size.getSize();

        // ---------------------- 4. SOFT DELETE ----------------------
        size.setDeleted(true);
        size.setActive(false);

        sizeRepository.save(size);

        // 2️⃣ FORCE product and variant total recalculation
        variant.recalculateTotalProductVariantQuantity();
        variantRepository.save(variant);

        product.recalculateTotalProductQuantity();
        // 3️⃣ Business rule: auto-unpublish if not sellable
        boolean unpublished = false;
        if (!product.hasSellableVariant()) {
            product.markUnpublished();
            unpublished = true;
        }
        productRepository.save(product);

        // Log admin activity
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String adminEmail = auth.getName();

        String adminRole = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() ->
                        new IllegalStateException("No role assigned to authenticated user.")
                );

        // Remove quotes if request body is a JSON string
        String cleanReason = reason.replace("\"", "").trim();

        String description = String.format(
                "Admin '%s' deleted size '%s' (Size ID: %d, Size SKU: %s) from Variant ID %d under Product ID %d. Reason: %s",
                adminEmail,
                loggedSize,
                loggedSizeId,
                loggedSizeSku,
                loggedVariantId,
                loggedProductId,
                cleanReason
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                loggedProductId,
                ProductActivityType.SIZE_DELETED,
                description,
                request
        );

        // ---------------------- 5. RETURN MESSAGE ----------------------
        return unpublished
                ?"Size deleted successfully, As this product has no active selling variants/sizes/images so its is marked as UNPUBLISHED."
                :"Size with ID " + sizeId + " has been deleted successfully(soft delete), can be restored later.";
    }

    @Override
    @Transactional
    public String restoreSize(Long sizeId, HttpServletRequest request) {

        // ---------------------- 1. VALIDATE INPUT ----------------------
        if (sizeId == null || sizeId <= 0) {
            throw new InvalidProductException("Size ID cannot be null");
        }

        // ---------------------- 2. FETCH SIZE (NON-DELETED ONLY) ----------------------
        ProductVariantSize size = sizeRepository.findByIdIncludeDeleted(sizeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with id: " + sizeId
                ));

        // ---------------------- 3. CHECK IF ALREADY DELETED ----------------------
        if (!size.isDeleted()) {
            throw new InvalidProductException(
                    "Size with id " + sizeId + " is already restored successfully."
            );
        }

        // -------------------- 4. Restore only if parent is NOT deleted ---------------------
        Long variantId = size.getVariant().getId();
        ProductVariant variant = variantRepository.findByIdIncludeDeleted(variantId)
                .orElseThrow(() -> new InvalidProductException("Parent variant not found."));

        if (variant.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot restore size because parent variant (ID: " + variantId + ") is deleted."
            );
        }

        // -------------------- 4. Restore only if parent product is NOT deleted ---------------------
        Long productId = variant.getProduct().getId();
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() -> new InvalidProductException("Parent product not found."));

        if (product.isDeleted()) {
            throw new InvalidProductException(
                    "Cannot restore size because parent product (ID: " + productId + ") is deleted."
            );
        }

        // Store values required for activity log BEFORE modifying entity.
        Long loggedProductId = product.getId();
        Long loggedVariantId = variant.getId();

        Long loggedSizeId = size.getId();
        String loggedSizeSku = size.getSizeSku();
        Size loggedSize = size.getSize();


        // ------------------- 5. Check if ANY active/inactive (non-deleted) size already exist------------
        // conflict will never be the case, because we are not leting the user creat size already existed in active/inactive/delete state
//        boolean exists = sizeRepository.existsByVariant_IdAndSizeAndIsDeletedFalse(
//                size.getVariant().getId(),
//                size.getSize()
//        );
//
//        if (exists) {
//            throw new InvalidProductException(
//                    "Cannot restore size. Another size with value '"
//                            + size.getSize() + "' already exists for this variant."
//            );
//        }


        // ---------------------- 5. RESTORE SIZE ----------------------
        size.setDeleted(false);
//        size.setActive(false);

        sizeRepository.save(size);

        // Log admin activity
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
                "Admin '%s' restored size '%s' (Size ID: %d, Size SKU: %s) for Variant ID %d under Product ID %d.",
                adminEmail,
                loggedSize,
                loggedSizeId,
                loggedSizeSku,
                loggedVariantId,
                loggedProductId
        );

        activityService.logActivity(
                adminEmail,
                adminRole,
                loggedProductId,
                ProductActivityType.SIZE_RESTORED,
                description,
                request
        );

        // ---------------------- 6. RETURN SUCCESS MESSAGE ----------------------
        return "Size with ID " + sizeId + " has been restored successfully.";

    }


    @Override
    public Page<DeletedSizeListResponseDTO> getDeletedSizesByVariantId(Long variantId, Pageable pageable){

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

        // ---------------------- 3. FETCH DELETED SIZES ----------------------
        Page<ProductVariantSize> deletedSizes =
                sizeRepository.findDeletedSizesByVariantId(
                        variantId,
                        pageable
                );

        // ---------------------- 4. MAP RESPONSE ----------------------
        return deletedSizes.map(size ->
                new DeletedSizeListResponseDTO(

                        size.getId(),
                        variantId,
                        variant.getProduct().getId(),
                        size.getSizeSku(),
                        size.getSize(),
                        size.isActive(),
                        size.isDeleted()
                )
        );
    }


    @Override // Active only
    public ProductVariantSizeResponseDTO getSizeBySkuForCustomer(String sizeSku) {

        // 1️⃣ Validate input
        if (sizeSku == null || sizeSku.trim().isEmpty()) {
            throw new InvalidProductException("Size SKU cannot be null or empty");
        }

        // 2️⃣ Fetch size (non-deleted because of @Where)
        ProductVariantSize size = sizeRepository.findBySizeSku(sizeSku.trim())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Size not found with SKU: " + sizeSku
                ));

        // 3️⃣ Ensure size is active for customers
        if (!size.isActive()) {
            throw new InvalidProductException(
                    "This size is currently inactive and cannot be viewed by customers."
            );
        }

        // 4️⃣ Parent variant must be active + not deleted
        ProductVariant variant = size.getVariant();

        if (variant == null || variant.isDeleted() || !variant.isActive()) {
            throw new InvalidProductException("This size belongs to an inactive or deleted variant.");
        }

        // 5️⃣ Validate parent product active + Not deleted + published(best practice)
        Product product = variant.getProduct();

        if (product.isDeleted() || !product.isActive() || !product.isPublished()) {
            throw new InvalidProductException(
                    "This size belongs to a product that is not available for customers."
            );
        }

        // 6️⃣ mapped full response
        return sizeMapper.toResponseDTO(size);

    }

    @Override
    public List<ProductVariantSizeListResponseDTO> searchBySize(Size size) {

        // 1️⃣ Validate input
        if (size == null) {
            throw new InvalidProductException("Size cannot be null");
        }

        // 2️⃣ Fetch all non-deleted sizes with this size enum
        List<ProductVariantSize> allSizes = sizeRepository.findBySize(size);

        // 3️⃣ Filter only CUSTOMER-visible sizes
        //    - size active
        //    - variant exists, not deleted, active
        //    - product exists, not deleted, active, published
        List<ProductVariantSize> validSizes = allSizes.stream()
                .filter(ProductVariantSize::isActive)
                .filter(s -> s.getVariant() != null && !s.getVariant().isDeleted() && s.getVariant().isActive())
                .filter(s -> {
                    Product p = s.getVariant().getProduct();
                    return p != null && !p.isDeleted() && p.isActive() && p.isPublished();
                })
                .toList();

        if (validSizes.isEmpty()) {
            throw new ProductNotFoundException("No sizes found for: " + size);
        }

        // 4️⃣ Return list response
        return sizeMapper.toListDTOs(validSizes);
    }

    @Override
    @Transactional(readOnly = true)
    public long countsizesByVariantId(Long variantId) {

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
        // 4️⃣ Count only sizes visible to customer
        return variant.getSizes().stream()
                .filter(s -> s.isActive() && !s.isDeleted())        // size active and not deleted
                .filter(s -> s.getQuantity() != null && s.getQuantity() > 0) // quantity not null/0
                .filter(s -> s.getVariant() != null  // size variant not null
                        && s.getVariant().isActive()   // size variant is active
                        && !s.getVariant().isDeleted()) // size variant is not deleted
                .filter(s -> {
                    Product p = s.getVariant().getProduct();
                    return p != null && p.isActive()
                            && !p.isDeleted() && p.isPublished();
                })
                .count();
    }
}
