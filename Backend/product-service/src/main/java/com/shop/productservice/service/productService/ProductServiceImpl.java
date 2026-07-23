package com.shop.productservice.service.productService;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.CreateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.UpdateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.*;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.CustomerProductVariantResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantResponseDTO;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import com.shop.productservice.exception.InvalidProductException;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.mapper.*;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantImageRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.repository.ProductVariantSizeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper  productVariantMapper;
    private final ProductVariantSizeMapper productVariantSizeMapper;
    private final ProductVariantImageMapper productVariantImageMapper;
    private final CustomerProductVariantImageMapper customerProductVariantImageMapper;
    private final CustomerProductVariantSizeMapper customerProductVariantSizeMapper;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantSizeRepository sizeRepository;
    private final ProductVariantImageRepository imageRepository;


    @Override
    public CreatedProductResponseDTO createProduct(CreateProductRequestDTO dto) {

        // ---------------- STEP 1: Check if ASIN already exists ----------------
        Optional<Product> existingProductOpt = productRepository.findByAsin(dto.getAsin().trim().toUpperCase()); //will only check for active/inactive

        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();

            // ✅ CASE A: Product exists AND is active -> reject creation
            if (existingProduct.isActive() && !existingProduct.isDeleted()) {
                throw new InvalidProductException(
                        "Product with ASIN " + dto.getAsin() + " already exists in your catalog."
                );
            }

            // ✅ CASE B: Product exists but is not active -> reactivate it with activateProduct()
            throw new InvalidProductException(
                    "Product with ASIN " + dto.getAsin() + " already exists but is IN-ACTIVE. " +
                            "Please reactivate it from product management instead of creating a new one."
            );
        }
        // ---------------- STEP 2: Map DTO -> Entity ----------------
        // Using MapStruct (ignores id, timestamps, variants)
        Product product = productMapper.toEntity(dto);

        // ---------------- STEP 3.1: Initialize basic fields ----------------
        product.setTotalProductQuantity(0); // no variants yet

        // ---------------- STEP 3.2: Handle null lists (tags/highlights) ----------------
        if (product.getTags() == null) {
            product.setTags(new ArrayList<>());
        }
        if (product.getHighlights() == null) {
            product.setHighlights(new ArrayList<>());
        }

        // ---------------- STEP 4: Save to DB (catch duplicate ASIN from soft-deleted rows)----------------
        try {
            Product saved = productRepository.save(product);

            // ---------------- STEP 5: Return Created Response DTO ----------------
            return productMapper.toCreatedResponseDTO(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Inspect cause/message to determine if it's a duplicate ASIN violation.
            // Common MySQL message: "Duplicate entry 'MD-HEEL-STLT-6083' for key 'products.idx_product_asin'"
            String rootMessage = Optional.ofNullable(ex.getMostSpecificCause())
                    .map(Throwable::getMessage)
                    .orElse(ex.getMessage());

            if (rootMessage != null && (rootMessage.contains("idx_product_asin") || rootMessage.toLowerCase().contains("duplicate entry"))) {
                // Provide a clear and actionable message to caller (no repo changes required)
                throw new InvalidProductException(
                        "Cannot create product: ASIN '" + dto.getAsin() +
                                "' already exists in the database (it may be soft-deleted). " +
                                "Please restore the existing product instead of creating a new one."
                );
            }
            // Not the ASIN unique-constraint—rethrow as a server error (preserve original stack)
            throw ex;
        }
    }

    @Override
    @Transactional
    public UpdateProductResponseDTO updateProduct(UpdateProductRequestDTO dto) {

        // ---------------- STEP 1: Validate product existence ----------------
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + dto.getProductId() + " not found"
                ));

        // ---------------- STEP 4: Prepare list to track updated fields ----------------
        List<String> updatedFields = new ArrayList<>();

        // ---------------- STEP 5: Partial field updates ----------------

        if (dto.getName() != null && !dto.getName().equals(product.getName())) {
            product.setName(dto.getName());
            updatedFields.add("name");
        }

        if (dto.getDescription() != null && !dto.getDescription().equals(product.getDescription())) {
            product.setDescription(dto.getDescription());
            updatedFields.add("description");
        }

        if (dto.getCategory() != null && !dto.getCategory().equals(product.getCategory())) {
            product.setCategory(dto.getCategory());
            updatedFields.add("category");
        }

        if (dto.getSubCategory() != null && !dto.getSubCategory().equals(product.getSubCategory())) {
            product.setSubCategory(dto.getSubCategory());
            updatedFields.add("subCategory");
        }

        if (dto.getBrand() != null && !dto.getBrand().equals(product.getBrand())) {
            product.setBrand(dto.getBrand());
            updatedFields.add("brand");
        }

        if (dto.getMaterial() != null && !dto.getMaterial().equals(product.getMaterial())) {
            product.setMaterial(dto.getMaterial());
            updatedFields.add("material");
        }

        if (dto.getGender() != null && dto.getGender() != product.getGender()) {
            product.setGender(dto.getGender());
            updatedFields.add("gender");
        }

        if (dto.getAgeGroup() != null && dto.getAgeGroup() != product.getAgeGroup()) {
            product.setAgeGroup(dto.getAgeGroup());
            updatedFields.add("ageGroup");
        }

        // ---------------- STEP 6: Replace tags completely if provided ----------------
        if (dto.getTags() != null) {
            product.setTags(dto.getTags());
            updatedFields.add("tags");
        }

        // ---------------- STEP 7: Replace highlights completely if provided ----------------
        if (dto.getHighlights() != null) {
            product.setHighlights(dto.getHighlights());
            updatedFields.add("highlights");
        }

        // ---------------- STEP 8: Save product not needed, used @Transactional , auto save the changes----------------
        productRepository.save(product);

        // ---------------- STEP 9: Build response DTO ----------------
        UpdateProductResponseDTO response = new UpdateProductResponseDTO();
        response.setProductId(product.getId());
        response.setAsin(product.getAsin());
        response.setName(product.getName());
        response.setUpdatedFields(updatedFields);

        // ---------------- STEP 10: check if list is not empty ----------------
        if (updatedFields.isEmpty()) {
            response.setMessage("No changes were applied");
        } else {
            response.setMessage("Product updated successfully");
        }
        return response;
    }

    @Override
    @Transactional
    public String publishProduct(Long productId) {

        // 1️⃣ Validate productId input
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch product from DB (must exist), only will check with non deleted products
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        if (product.isPublished()) {
            throw new InvalidProductException(
                    "This product is already published."
            );
        }

        // 4️⃣ Product must be ACTIVE to be published
        // Reason: Activation & publishing are two separate workflows.
        if (!product.isActive()) {
            throw new InvalidProductException(
                    "Product is inactive. Activate it before publishing."
            );
        }

        // 5️⃣ Product MUST have at least one variant
        // rule: Cannot publish product with zero variants.
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            throw new InvalidProductException(
                    "Cannot publish product. Add at least one active variant."
            );
        }

        // 6️⃣ Validate every ACTIVE variant
        // A product is publishable only if:
        // - variant is active
        // - variant has at least one active size
        // - variant has at least one active image
        boolean hasAnyActiveVariant = false;
        boolean hasVariantWithActiveSize = false;
        boolean hasVariantWithActiveImage = false;
        boolean hasValidVariant = false;

        for (ProductVariant variant : product.getVariants()) {

            // Step 6.1 — variant must be ACTIVE
            if (!variant.isActive() || variant.isDeleted()) {
                continue;
            }
            hasAnyActiveVariant = true;

            // Step 6.2 — Variant must have at least ONE sellable size.
            // A sellable size means:
            // ✔ Active
            // ✔ Not Deleted
            // ✔ Quantity greater than zero
            // Reason:
            // Publishing a product that has no stock makes no business sense.
            // At least one size should be available for purchase.
            boolean activeSizeExists = variant.getSizes() != null &&
                    variant.getSizes().stream().anyMatch(size ->
                            size.isActive()
                                    && !size.isDeleted()
                                    && size.getQuantity() > 0
                    );

            if (activeSizeExists) {
                hasVariantWithActiveSize = true;
            } else {
                continue;   // no size → cannot be a valid variant
            }

            // Step 6.3 — variant must have AT LEAST 1 active image
            boolean activeImageExists = variant.getImages() != null &&
                    variant.getImages().stream().anyMatch(image ->
                            image.isActive() && !image.isDeleted()
                    );

            if (activeImageExists) {
                hasVariantWithActiveImage = true;
            } else {
                continue;   // no image → cannot be a valid variant
            }
            // If all checks passed → this variant is valid for publishing
            hasValidVariant = true;
            break;
        }

        // 7️⃣ Now throw specific exceptions based on what was missing
        if (!hasAnyActiveVariant) {
            throw new InvalidProductException(
                    "Cannot publish product. No active variants found."
            );
        }

        if (!hasVariantWithActiveSize) {
            throw new InvalidProductException(
                    "Cannot publish product. At least one active variant must have an active size with available stock."
            );
        }

        if (!hasVariantWithActiveImage) {
            throw new InvalidProductException(
                    "Cannot publish product. No active images found in any active variant."
            );
        }

        if (!hasValidVariant) {
            throw new InvalidProductException(
                    "Cannot publish product. At least one variant must have: one active size AND one active image."
            );
        }

        // 7) Mark published
        product.markPublished(Instant.now());
        productRepository.save(product);

        return "Product published successfully!";
    }

    @Override
    @Transactional
    public String unpublishProduct(Long productId) {

        // 1️⃣ Validate productId input
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch product from DB (must exist), only will check with non deleted products
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        if (!product.isPublished()) {
            throw new InvalidProductException(
                    "This product is already not published."
            );
        }
        // 7) Mark published
        product.markUnpublished();
        productRepository.save(product);

        return "Product unpublished successfully!";
    }

    @Override
    public ProductResponseDTO getProductById(Long productId) {

        // 1️⃣ Validate input
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID");
        }

        // 2️⃣ Fetch product (admin can access active + inactive)
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        // 3️⃣ Block deleted products
//        if (product.isDeleted()) {
//            throw new ProductNotFoundException("Product not found or has been deleted");
//        }
        // 4️⃣ Map to response DTO
        return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO getProductByAsin(String asin) {

        // 1️⃣ Validate input
        if (asin == null || asin.trim().isEmpty()) {
            throw new InvalidProductException("ASIN cannot be null or empty");
        }

        // 2️⃣ Fetch product by ASIN (admin can access active + inactive)
        // trim used in case any trailing/leading space, newline, tab
        Product product = productRepository.findByAsin(asin.toUpperCase().trim())
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ASIN: " + asin));

        // 4️⃣ Convert entity -> full response DTO
        return productMapper.toResponseDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getDeletedProductById(Long productId) {
        // deleted product with its deleted variant/images and sizes

        // 1️⃣ Validate input
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID");
        }
        // 2️⃣ Fetch product(active/inactive/deleted)
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        // 3️⃣ Ensure it is deleted
        if (!product.isDeleted()) {
            throw new ProductNotFoundException("Product is not deleted. Use getProductById() instead.");
        }

        List<ProductVariant> deletedVariants =
                variantRepository.findByProductIdIncludeDeleted(productId)
                        .stream()
                        .filter(ProductVariant::isDeleted)
                        .toList();

        List<ProductVariantResponseDTO> variantDTOs = new ArrayList<>();

        for (ProductVariant variant : deletedVariants) {

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

            ProductVariantResponseDTO variantDTO =
                    productVariantMapper.toResponseDTO(variant);

            variantDTO.setSizes(sizeDTOs);
            variantDTO.setImages(imageDTOs);

            variantDTOs.add(variantDTO);
        }
        ProductResponseDTO response = productMapper.toResponseDTO(product);

        response.setVariants(variantDTOs);

        // 4️⃣ Return DTO
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeletedProductListResponseDTO> getAllDeletedProducts(Pageable pageable) {

        // 1️⃣ Fetch deleted products (DB-level pagination, same query/table as before)
        Page<Product> deletedPage = productRepository.findAllDeleted(pageable);

        // 2️⃣ If no records on first page, throw not-found
        if (deletedPage.isEmpty()) {
            throw new ProductNotFoundException("No deleted product found.");
        }

        // 3️⃣ Map Product -> DTO (same fields as before, Page.map preserves pagination metadata)
        return deletedPage.map(product -> new DeletedProductListResponseDTO(
                product.getId(),
                product.getAsin(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.isActive(),
                product.isDeleted()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getAllProducts(Pageable pageable) {

        // 1️⃣ Fetch all non-deleted products (admin view) — sort hardcoded in method name (created DESC)
        Page<Product> products = productRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);

        // 2️⃣ If no records on first page, throw not-found
        if (products.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        // 3️⃣ Map to list response DTOs (Page.map preserves pagination metadata)
        return products.map(productMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getAllPublishedProducts(Pageable pageable) {

        // 1️⃣ Fetch all published products (active only, sort hardcoded: publishedAt DESC)
        Page<Product> products = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalseOrderByPublishedAtDesc(pageable);

        // 2️⃣ If no records on this page, throw not-found
        if (products.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        // 3️⃣ Map to list response DTOs (Page.map preserves pagination metadata)
        return products.map(productMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getAllReadyToPublishProducts(Pageable pageable) {

        // ------------------------------------------------------------------
        // Fetch products that satisfy ALL publish conditions.
        //
        // The repository query already ensures:
        //
        // ✔ Product is NOT published
        // ✔ Product is ACTIVE
        // ✔ Product is NOT deleted
        // ✔ At least one ACTIVE & NON-DELETED Variant exists
        // ✔ That Variant has at least one ACTIVE & NON-DELETED Size
        // ✔ That Size has quantity greater than zero
        // ✔ That Variant has at least one ACTIVE & NON-DELETED Image
        //
        // Therefore, every product returned from this query is immediately
        // eligible for publishing according to the business rules.
        // ------------------------------------------------------------------
        Page<Product> products = productRepository.findAllReadyToPublishProducts(pageable);

        // ------------------------------------------------------------------
        // If no products satisfy the publish criteria,
        // return a meaningful exception.
        // ------------------------------------------------------------------
        if (products.isEmpty()) {
            throw new InvalidProductException(
                    "No products are ready to publish."
            );
        }

        // ------------------------------------------------------------------
        // Convert Product entities into ProductListResponseDTO.
        //
        // Page.map() automatically preserves:
        // • Current page number
        // • Page size
        // • Total number of elements
        // • Total number of pages
        //
        // while mapping each Product entity into its corresponding DTO.
        // ------------------------------------------------------------------
        return products.map(productMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponseDTO> getRecentlyPublishedProducts(int limit) {

        // 1️⃣ Validate limit
        if (limit <= 0) {
            limit = 10; // default
        }

        // 2️⃣ Fetch recent published products sorted by published at DESC
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));

        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);

        // 3️⃣ Return empty list if none
        if (products.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        // 4️⃣ Map to list DTO
        return productMapper.toListDTOs(products.getContent());
    }

    @Override
    public CustomerProductResponseDTO getActiveProductByAsin(String asin) {

        // ---------------------------------------------------------
        // STEP 1
        // Validate ASIN.
        //
        // Reject null, empty or whitespace-only values.
        // ---------------------------------------------------------
        if (asin == null || asin.trim().isEmpty()) {
            throw new InvalidProductException("ASIN cannot be null or empty.");
        }

        // ---------------------------------------------------------
        // STEP 2
        // Fetch customer-visible product.
        //
        // Repository already guarantees:
        //
        // ✔ Published
        // ✔ Active
        // ✔ Not Deleted
        //
        // Therefore if no record is found,
        // customer should receive Product Not Found.
        // ---------------------------------------------------------
        Product product = productRepository
                .findByAsinAndIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(
                        asin.trim()
                )
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with ASIN: " + asin
                        )
                );

        // ---------------------------------------------------------
        // STEP 3
        // Build customer response.
        //
        // This method:
        //
        // ✔ Filters inactive/deleted variants.
        // ✔ Filters inactive/deleted/out-of-stock sizes.
        // ✔ Filters inactive/deleted images.
        // ✔ Removes invalid variants.
        //
        // NOTE:
        // Product entity itself is NEVER modified.
        // ---------------------------------------------------------
        CustomerProductResponseDTO response = buildCustomerProductResponse(product);

        // ---------------------------------------------------------
        // STEP 4
        // Safety validation.
        //
        // Although repository guarantees a published product,
        // it is possible that after filtering:
        //
        // • every variant becomes invalid
        // • every size is out of stock
        // • every image becomes inactive
        //
        // In such a case the product should not be exposed
        // to customers.
        // ---------------------------------------------------------
        if (response.getVariants() == null || response.getVariants().isEmpty()) {
            throw new ProductNotFoundException(
                    "Product is currently unavailable."
            );
        }

        // ---------------------------------------------------------
        // STEP 5
        // Return customer-facing DTO.
        // ---------------------------------------------------------
        return response;

    }

    @Override
    @Transactional
    public String deactivateProduct(Long productId) {
        // 1️⃣ Validate productId
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch product from DB, will not check for deleted product
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId + " or may be deleted"));

        // 4️⃣ If already inactive → no action needed
        if (!product.isActive()) {
            return "Product is already decativated.";
        }
        // 5️⃣ Deactivate the product (soft hide)
        // DO NOT deactivate variants, sizes, or images
        // Just hide the parent product.
        product.setActive(false);
        product.markUnpublished(); // cannot keep published if inactive

        productRepository.save(product);

        // 6️⃣ Success message
        return "Product deactivated successfully.";
    }

    @Override
    @Transactional
    public String activateProduct(Long productId) {

        // 1️⃣ Validate productId input
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch product from DB, will not check for deleted product
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));


        // 4️⃣ Already active → no action needed
        if (product.isActive()) {
            return "Product is already active.";
        }

        // 5️⃣ Activate the product
        product.setActive(true);

        // IMPORTANT:
        // Activating a product does NOT publish it.
//        product.markUnpublished();

        productRepository.save(product);

        // 6️⃣ Provide reactivation summary
        return "Product activated successfully. "
                + "Note: Activate or review its variants, sizes, and images before publishing.";
    }

    @Override
    @Transactional
    public String deleteProduct(Long productId) {

        // 1️⃣ Validate productId
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch product, will also check for deleted product
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        // 3️⃣ Already deleted? No need to delete again
        if (product.isDeleted()) {
            return "Product is already deleted.";
        }

        // 4️⃣ Soft delete the product
        product.setDeleted(true);      // product is now soft-deleted
        product.setActive(false);      // inactive so admin doesn't modify it accidentally
        product.markUnpublished();     // customers cannot see deleted product

        // 5️⃣ delete all VARIANTS and mark deleted
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setActive(false);
                variant.setDeleted(true);

                // 6️⃣ Deactivate all SIZES
                if (variant.getSizes() != null) {
                    for (ProductVariantSize size : variant.getSizes()) {
                        size.setActive(false);
                        size.setDeleted(true);
                    }
                }

                // 7️⃣ Deactivate all IMAGES
                if (variant.getImages() != null) {
                    for (ProductVariantImage image : variant.getImages()) {
                        image.setActive(false);
                        image.setDeleted(true);
                    }
                }
            }
        }
        // 8️⃣ Save the product (cascading will update variants & sizes)
        productRepository.save(product);

        // 9️⃣ Return confirmation
        return "Product deleted successfully (soft delete), Can be restored later";
    }

    @Override
    @Transactional
    public String restoreDeletedProduct(Long productId) {

        // 1️⃣ Validate input
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        // 2️⃣ Fetch product, with including deleted, because @where will not fetch deleted with findById
        // findDeletedById will only check for deleted products.
        Product product = productRepository.findByIdIncludeDeleted(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with ID: " + productId));

        // 3️⃣ Already restored? No need to restore again
        if (!product.isDeleted()) {
            return "Product is not deleted, maybe already restored successfully.";
        }

        //  3️⃣ Check if ASIN already exists with any newly created product
        // nevere will this case.....
//        if (productRepository.existsByAsinAndIsDeletedFalse(product.getAsin())) {
//            throw new InvalidProductException(
//                    "Cannot restore product. Another active product already exists with ASIN: " + product.getAsin()
//            );
//        }

        // 4️⃣ Restore the product
        product.setDeleted(false);      // Product is restored
//        product.setActive(false);       // BUT not active, admin will activate manually
//        product.markUnpublished();      // Must publish again after activation

        // 5️⃣ Do NOT restore variants/sizes/images automatically
        // Admin will choose which ones to re-activate
        // THEY REMAIN:
        // isDeleted = false (already)
        // isActive = false (from deletion)
        // This is correct real-world behaviour!

        productRepository.save(product);
        return "Product restored successfully. Activate it, before publishing.";
    }

    @Override
    public List<ProductListResponseDTO> searchByName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductException("Provided name is null or empty.");
        }

        String keyword = name.trim();

        // 1️⃣ Get published products only
        List<Product> published = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

        // 2️⃣ Filter by name contains keyword (case-insensitive)
        List<Product> filtered = published.stream()
                .filter(p -> p.getName() != null &&
                        p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        // 3️⃣ Return empty list if none
        if (filtered.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        // 3️⃣ Map to response DTO
        return productMapper.toListDTOs(filtered);
    }


    @Override
    public List<ProductListResponseDTO> getByCategory(String category) {

        if (category == null || category.trim().isEmpty()) {
            throw new InvalidProductException("Provided category is null or empty.");
        }

        String cat = category.trim().toLowerCase();

        List<Product> published = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

        List<Product> filtered = published.stream()
                .filter(p -> p.getCategory() != null &&
                        p.getCategory().toLowerCase().contains(cat))   // ▼ PARTIAL MATCH
                .toList();

        // 3️⃣ Return empty list if none
        if (filtered.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(filtered);
    }


    @Override
    public List<ProductListResponseDTO> getByBrand(String brand) {

        if (brand == null || brand.trim().isEmpty()) {
            throw new InvalidProductException("Provided brand is null or empty.");
        }

        String br = brand.trim().toLowerCase();

        List<Product> published = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

        List<Product> filtered = published.stream()
                .filter(p -> p.getBrand() != null &&
                        p.getBrand().toLowerCase().contains(br))
                .toList();

        // 3️⃣ Return empty list if none
        if (filtered.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(filtered);
    }


    @Override
    public List<ProductListResponseDTO> getByCategoryAndSubCategory(String category, String subCategory) {

        if (category == null || subCategory == null ||
                category.trim().isEmpty() || subCategory.trim().isEmpty()) {
            throw new InvalidProductException("Provided category or sub category is null or empty.");
        }

        String cat = category.trim();
        String sub = subCategory.trim();

        List<Product> products = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalseAndCategoryContainingIgnoreCaseAndSubCategoryContainingIgnoreCaseOrderByPublishedAtDesc(cat, sub);

        // 3️⃣ Return empty list if none
        if (products.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(products);
    }

    @Override
    public List<ProductListResponseDTO> getByGender(Gender gender) {
        if (gender == null) {
            throw new InvalidProductException("Provided gender is null or empty.");
        }

        List<Product> published = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

        List<Product> filtered = published.stream()
                .filter(p -> p.getGender() == gender)
                .toList();

        // 3️⃣ Return empty list if none
        if (filtered.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(filtered);
    }


    @Override
    public List<ProductListResponseDTO> getByAgeGroup(AgeGroup ageGroup) {
        if (ageGroup == null) {
            throw new InvalidProductException("Provided AgeGroup is null or empty.");
        }

        List<Product> published = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

        List<Product> filtered = published.stream()
                .filter(p -> p.getAgeGroup() == ageGroup)
                .toList();

        // 3️⃣ Return empty list if none
        if (filtered.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(filtered);
    }


    @Override
    public List<ProductListResponseDTO> searchByTag(String tag) {

        if (tag == null || tag.trim().isEmpty()) {
            throw new InvalidProductException("Provided tag is null or empty.");
        }

        String keyword = tag.trim().toLowerCase();

        List<Product> published = productRepository
                .findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

        List<Product> filtered = published.stream()
                .filter(p -> p.getTags() != null &&
                        p.getTags().stream()
                                .anyMatch(t -> t.toLowerCase().contains(keyword)))
                .toList();

        // 3️⃣ Return empty list if none
        if (filtered.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponseDTO> filterProducts(String category,
                                                       String subCategory,
                                                       Gender gender,
                                                       String tag) {
        String cat = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String sub = (subCategory == null || subCategory.trim().isEmpty()) ? null : subCategory.trim();
        String tg = (tag == null || tag.trim().isEmpty()) ? null : tag.trim();


        List<Product> products = productRepository.filterProducts(cat, sub, gender, tg);

        if (products.isEmpty()) {
            throw new InvalidProductException("No product found.");
        }

        return productMapper.toListDTOs(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponseDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max) {

        if (min == null || max == null || min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(min) < 0) {
            throw new InvalidProductException("Invalid price range.");
        }

        List<Product> products = productRepository.findProductsByPriceRange(min, max);

        if (products.isEmpty()) throw new InvalidProductException("No product found.");

        return productMapper.toListDTOs(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponseDTO> getRelatedProducts(Long productId) {
        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        List<Product> relatedProducts = productRepository.findRelatedProducts(
                productId,
                product.getCategory(),
                product.getBrand()
        );

        if (relatedProducts.isEmpty()) {
            return new ArrayList<>();
        }

        return productMapper.toListDTOs(relatedProducts.stream()
                .limit(6)
                .toList());
    }

    // ============ PAGINATED CUSTOMER ENDPOINTS ============

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getRecentlyPublishedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        return products.map(productMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> searchByName(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductException("Provided name is null or empty.");
        }
        String keyword = name.trim().toLowerCase();
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        List<Product> filtered = products.getContent().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(keyword))
                .toList();
        List<ProductListResponseDTO> dtos = productMapper.toListDTOs(filtered);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidProductException("Provided category is null or empty.");
        }
        String cat = category.trim().toLowerCase();
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        List<Product> filtered = products.getContent().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().toLowerCase().contains(cat))
                .toList();
        List<ProductListResponseDTO> dtos = productMapper.toListDTOs(filtered);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getByCategoryAndSubCategory(String category, String subCategory, Pageable pageable) {
        if (category == null || subCategory == null || category.trim().isEmpty() || subCategory.trim().isEmpty()) {
            throw new InvalidProductException("Provided category or sub category is null or empty.");
        }
        String cat = category.trim();
        String sub = subCategory.trim();
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalseAndCategoryContainingIgnoreCaseAndSubCategoryContainingIgnoreCaseOrderByPublishedAtDesc(pageable, cat, sub);
        return products.map(productMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getByBrand(String brand, Pageable pageable) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new InvalidProductException("Provided brand is null or empty.");
        }
        String br = brand.trim().toLowerCase();
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        List<Product> filtered = products.getContent().stream()
                .filter(p -> p.getBrand() != null && p.getBrand().toLowerCase().contains(br))
                .toList();
        List<ProductListResponseDTO> dtos = productMapper.toListDTOs(filtered);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getByGender(Gender gender, Pageable pageable) {
        if (gender == null) {
            throw new InvalidProductException("Provided gender is null.");
        }
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        List<Product> filtered = products.getContent().stream()
                .filter(p -> p.getGender() == gender)
                .toList();
        List<ProductListResponseDTO> dtos = productMapper.toListDTOs(filtered);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getByAgeGroup(AgeGroup ageGroup, Pageable pageable) {
        if (ageGroup == null) {
            throw new InvalidProductException("Provided age group is null.");
        }
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        List<Product> filtered = products.getContent().stream()
                .filter(p -> p.getAgeGroup() == ageGroup)
                .toList();
        List<ProductListResponseDTO> dtos = productMapper.toListDTOs(filtered);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> searchByTag(String tag, Pageable pageable) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new InvalidProductException("Provided tag is null or empty.");
        }
        String keyword = tag.trim().toLowerCase();
        Page<Product> products = productRepository.findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(pageable);
        List<Product> filtered = products.getContent().stream()
                .filter(p -> p.getTags() != null && p.getTags().stream()
                        .anyMatch(t -> t.toLowerCase().contains(keyword)))
                .toList();
        List<ProductListResponseDTO> dtos = productMapper.toListDTOs(filtered);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> filterProducts(String category, String subCategory, Gender gender, String tag, Pageable pageable) {
        String cat = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String sub = (subCategory == null || subCategory.trim().isEmpty()) ? null : subCategory.trim();
        String tg = (tag == null || tag.trim().isEmpty()) ? null : tag.trim();

        Page<Product> products = productRepository.filterProductsPageable(pageable, cat, sub, gender, tg);
        return products.map(productMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max, Pageable pageable) {
        if (min == null || max == null || min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(min) < 0) {
            throw new InvalidProductException("Invalid price range.");
        }
        Page<Product> products = productRepository.findProductsByPriceRangePageable(pageable, min, max);
        return products.map(productMapper::toListDTO);
    }

    /**
     * Builds a customer-facing product response.
     *
     * ------------------------------------------------------------------
     * WHY THIS METHOD EXISTS
     * ------------------------------------------------------------------
     *
     * Admin APIs and Customer APIs have different visibility rules.
     *
     * Admin:
     *      Can view everything.
     *
     * Customer:
     *      Should ONLY receive data that can actually be purchased.
     *
     * Instead of modifying the Product entity,
     * this method filters the child collections while building the
     * CustomerProductResponseDTO.
     *
     * Therefore:
     *
     * ✔ No JPA entity is modified.
     * ✔ No accidental database update.
     * ✔ Product remains unchanged.
     *
     * ------------------------------------------------------------------
     * BUSINESS RULES
     * ------------------------------------------------------------------
     *
     * Product
     * --------
     * Repository already guarantees:
     *
     * ✔ Published
     * ✔ Active
     * ✔ Not Deleted
     *
     * Therefore Product itself is NOT filtered here.
     *
     *
     * Variant
     * --------
     * Keep only variants that are:
     *
     * ✔ Active
     * ✔ Not Deleted
     *
     *
     * Size
     * -----
     * Keep only sizes that are:
     *
     * ✔ Active
     * ✔ Not Deleted
     * ✔ Quantity > 0
     *
     *
     * Image
     * ------
     * Keep only images that are:
     *
     * ✔ Active
     * ✔ Not Deleted
     *
     *
     * Final Variant Validation
     * -------------------------
     *
     * Variant must contain:
     *
     * ✔ At least one valid Size
     * ✔ At least one valid Image
     *
     * Otherwise that Variant is skipped.
     *
     * ------------------------------------------------------------------
     * @param product Published Product entity
     * @return CustomerProductResponseDTO
     */
//    private CustomerProductResponseDTO buildCustomerProductResponse(Product product) {
//
//        if (product == null) {
//            return null;
//        }
//
//        // ---------------------------------------------------------
//        // This list will contain only customer-visible variants.
//        // ---------------------------------------------------------
//        List<CustomerProductVariantResponseDTO> customerVariants = new ArrayList<>();
//
//        if (product.getVariants() != null) {
//
//            for (ProductVariant variant : product.getVariants()) {
//
//                // -------------------------------------------------
//                // Skip inactive or deleted variants.
//                // -------------------------------------------------
//                if (!variant.isActive() || variant.isDeleted()) {
//                    continue;
//                }
//
//                // -------------------------------------------------
//                // Filter customer-visible sizes.
//                // -------------------------------------------------
//                List<ProductVariantSize> visibleSizes = new ArrayList<>();
//                if (variant.getSizes() != null) {
//
//                    for (ProductVariantSize size : variant.getSizes()) {
//
//                        if (!size.isActive()) {
//                            continue;
//                        }
//
//                        if (size.isDeleted()) {
//                            continue;
//                        }
//
//                        if (size.getQuantity() == null ||
//                                size.getQuantity() <= 0) {
//                            continue;
//                        }
//
//                        visibleSizes.add(size);
//                    }
//                }
//
//
//                // -------------------------------------------------
//                // Filter customer-visible images.
//                // -------------------------------------------------
//                List<ProductVariantImage> visibleImages = new ArrayList<>();
//
//                if (variant.getImages() != null) {
//
//                    for (ProductVariantImage image : variant.getImages()) {
//
//                        if (!image.isActive()) {
//                            continue;
//                        }
//
//                        if (image.isDeleted()) {
//                            continue;
//                        }
//
//                        visibleImages.add(image);
//                    }
//                }
//
//                // -------------------------------------------------
//                // Variant must have:
//                //
//                // ✔ At least one sellable Size
//                // ✔ At least one visible Image
//                //
//                // Otherwise customer should never receive it.
//                // -------------------------------------------------
//                if (visibleSizes.isEmpty() || visibleImages.isEmpty()) {
//                    continue;
//                }
//
//                // -------------------------------------------------
//                // Build Customer Variant DTO
//                // -------------------------------------------------
//                CustomerProductVariantResponseDTO customerVariant =
//                        CustomerProductVariantResponseDTO.builder()
//                                .id(variant.getId())
//                                .skuCode(variant.getSkuCode())
//                                .variantName(variant.getVariantName())
//                                .color(variant.getColor())
//                                .sizes(customerProductVariantSizeMapper.toResponseDTOs(visibleSizes))
//                                .images(customerProductVariantImageMapper.toResponseDTOs(visibleImages))
//                                .build();
//
//                customerVariants.add(customerVariant);
//            }
//
//        }
//        // ---------------------------------------------------------
//        // Build Customer Product DTO
//        // ---------------------------------------------------------
//        return CustomerProductResponseDTO.builder()
//                .id(product.getId())
//                .asin(product.getAsin())
//                .name(product.getName())
//                .description(product.getDescription())
//                .category(product.getCategory())
//                .subCategory(product.getSubCategory())
//                .brand(product.getBrand())
//                .material(product.getMaterial())
//                .gender(product.getGender())
//                .ageGroup(product.getAgeGroup())
//                .tags(product.getTags())
//                .highlights(product.getHighlights())
//                .variants(customerVariants)
//                .build();
//    }
    private CustomerProductResponseDTO buildCustomerProductResponse(Product product) {

        if (product == null) {
            return null;
        }

        System.out.println("\n================ PRODUCT DEBUG ================");
        System.out.println("Product ID   : " + product.getId());
        System.out.println("Product Name : " + product.getName());

        if (product.getVariants() != null) {
            System.out.println("Total variants loaded = " + product.getVariants().size());

            for (ProductVariant v : product.getVariants()) {
                System.out.println("--------------------------------");
                System.out.println("Variant ID   : " + v.getId());
                System.out.println("SKU          : " + v.getSkuCode());
                System.out.println("Color        : " + v.getColor());
                System.out.println("Active       : " + v.isActive());
                System.out.println("Deleted      : " + v.isDeleted());
            }
        }

        // ---------------------------------------------------------
        // This list will contain only customer-visible variants.
        // ---------------------------------------------------------
        List<CustomerProductVariantResponseDTO> customerVariants = new ArrayList<>();

        if (product.getVariants() != null) {

            for (ProductVariant variant : product.getVariants()) {

                System.out.println("\n======================================");
                System.out.println("Processing Variant : " + variant.getSkuCode());

                // -------------------------------------------------
                // Skip inactive or deleted variants.
                // -------------------------------------------------
                if (!variant.isActive()) {
                    System.out.println("❌ Skipped -> Variant is INACTIVE");
                    continue;
                }

                if (variant.isDeleted()) {
                    System.out.println("❌ Skipped -> Variant is DELETED");
                    continue;
                }

                // -------------------------------------------------
                // Filter customer-visible sizes.
                // -------------------------------------------------
                List<ProductVariantSize> visibleSizes = new ArrayList<>();

                if (variant.getSizes() != null) {

                    System.out.println("Total Sizes = " + variant.getSizes().size());

                    for (ProductVariantSize size : variant.getSizes()) {

                        System.out.println(
                                "Checking Size -> "
                                        + size.getSize()
                                        + " | Qty=" + size.getQuantity()
                                        + " | Active=" + size.isActive()
                                        + " | Deleted=" + size.isDeleted()
                        );

                        if (!size.isActive()) {
                            System.out.println("   ❌ Size skipped (Inactive)");
                            continue;
                        }

                        if (size.isDeleted()) {
                            System.out.println("   ❌ Size skipped (Deleted)");
                            continue;
                        }

                        if (size.getQuantity() == null || size.getQuantity() <= 0) {
                            System.out.println("   ❌ Size skipped (Qty <= 0)");
                            continue;
                        }

                        System.out.println("   ✅ Size Added");

                        visibleSizes.add(size);
                    }
                }

                System.out.println("Visible Sizes = " + visibleSizes.size());

                // -------------------------------------------------
                // Filter customer-visible images.
                // -------------------------------------------------
                List<ProductVariantImage> visibleImages = new ArrayList<>();

                if (variant.getImages() != null) {

                    System.out.println("Total Images = " + variant.getImages().size());

                    for (ProductVariantImage image : variant.getImages()) {

                        System.out.println(
                                "Checking Image -> "
                                        + image.getId()
                                        + " | Active=" + image.isActive()
                                        + " | Deleted=" + image.isDeleted()
                        );

                        if (!image.isActive()) {
                            System.out.println("   ❌ Image skipped (Inactive)");
                            continue;
                        }

                        if (image.isDeleted()) {
                            System.out.println("   ❌ Image skipped (Deleted)");
                            continue;
                        }

                        System.out.println("   ✅ Image Added");

                        visibleImages.add(image);
                    }
                }

                System.out.println("Visible Images = " + visibleImages.size());

                // -------------------------------------------------
                // Final Validation
                // -------------------------------------------------
                if (visibleSizes.isEmpty()) {
                    System.out.println("❌ Variant skipped because NO visible sizes.");
                    continue;
                }

                if (visibleImages.isEmpty()) {
                    System.out.println("❌ Variant skipped because NO visible images.");
                    continue;
                }

                // -------------------------------------------------
                // Build DTO
                // -------------------------------------------------
                CustomerProductVariantResponseDTO customerVariant =
                        CustomerProductVariantResponseDTO.builder()
                                .id(variant.getId())
                                .skuCode(variant.getSkuCode())
                                .variantName(variant.getVariantName())
                                .color(variant.getColor())
                                .sizes(customerProductVariantSizeMapper.toResponseDTOs(visibleSizes))
                                .images(customerProductVariantImageMapper.toResponseDTOs(visibleImages))
                                .build();

                System.out.println("✅ Adding Variant DTO : " + customerVariant.getSkuCode());

                customerVariants.add(customerVariant);

                System.out.println("Current DTO Variant Count = " + customerVariants.size());
            }
        }

        System.out.println("\n================ FINAL RESULT ================");
        System.out.println("Total Variants Returned = " + customerVariants.size());

        for (CustomerProductVariantResponseDTO dto : customerVariants) {
            System.out.println(
                    "Returned Variant -> "
                            + dto.getId()
                            + " | "
                            + dto.getSkuCode()
                            + " | "
                            + dto.getColor()
            );
        }

        System.out.println("==============================================\n");

        // ---------------------------------------------------------
        // Build Customer Product DTO
        // ---------------------------------------------------------
        return CustomerProductResponseDTO.builder()
                .id(product.getId())
                .asin(product.getAsin())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .subCategory(product.getSubCategory())
                .brand(product.getBrand())
                .material(product.getMaterial())
                .gender(product.getGender())
                .ageGroup(product.getAgeGroup())
                .tags(product.getTags())
                .highlights(product.getHighlights())
                .variants(customerVariants)
                .build();
    }
}
