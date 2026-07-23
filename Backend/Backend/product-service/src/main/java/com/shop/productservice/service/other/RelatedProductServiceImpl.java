package com.shop.productservice.service.other;

import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.ProductListResponseDTO;
import com.shop.productservice.DTOs.RelatedProductDTOs.RequestDTOs.AddRelatedProductRequest;
import com.shop.productservice.DTOs.RelatedProductDTOs.ResponseDTOs.RelatedProductResponse;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.RelatedProduct;
import com.shop.productservice.exception.InvalidProductException;
import com.shop.productservice.exception.ProductNotFoundException;
import com.shop.productservice.mapper.ProductMapper;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.repository.RelatedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatedProductServiceImpl implements RelatedProductService {

    private final RelatedProductRepository relatedProductRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public RelatedProductResponse getRelatedProduct(Long productId, Long relatedVariantId) {

        if (productId == null || productId <= 0) {
            throw new InvalidProductException("Invalid product ID.");
        }

        if (relatedVariantId == null || relatedVariantId <= 0) {
            throw new InvalidProductException("Invalid related variant ID.");
        }

        RelatedProduct relatedProduct = relatedProductRepository
                .findByProduct_IdAndRelatedProductVariant_Id(
                        productId,
                        relatedVariantId
                )
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Related product not found."
                        )
                );

        return mapToResponse(relatedProduct);
    }

    @Override
    @Transactional
    public RelatedProductResponse addRelatedProduct(AddRelatedProductRequest request) {
        Long productId = request.getProductId();
        Long relatedVariantId = request.getRelatedProductVariantId();
        Integer displayOrder = request.getDisplayOrder();

        // 1. Fetch Main Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Main product not found with ID: " + productId));

        // 2. Fetch the target Variant (ensures exact color match accuracy)
        ProductVariant relatedVariant = productVariantRepository.findById(relatedVariantId)
                .orElseThrow(() -> new ProductNotFoundException("Product variant not found with ID: " + relatedVariantId));

        // 3. Business Validation: Prevent linking a product page to one of its own variants
        if (relatedVariant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Cannot link a product variant back to its own parent product.");
        }

        // 4. Data Integrity: Check if variant is already tied to this look in active/inactive/delete state
        Optional<RelatedProduct> existingRelationOpt = relatedProductRepository.findEvenIfDeleted(productId, relatedVariantId);

        if (existingRelationOpt.isPresent()) {
            RelatedProduct existingRelation = existingRelationOpt.get();

            // Check state and throw the separate, targeted exceptions
            if (!existingRelation.isDeleted()&&!existingRelation.isActive()) {
                throw new IllegalStateException("Validation Failed: This product variant relationship is already exist, currently inactive, Activate insted new creation.");
            }else if (!existingRelation.isDeleted()&&existingRelation.isActive()) {
                throw new IllegalStateException("Validation Failed: This product variant relationship is already active.");
            }
            else {
                throw new IllegalStateException("Validation Failed: This relationship already exists in a deleted state. Duplicate row insertion blocked.");
            }
        }

        // 5. Layout Validation: Ensure display slot isn't occupied by another active item
        Optional<RelatedProduct> existingOrderOpt = relatedProductRepository.findDisplayOrderEvenIfDeleted(productId, displayOrder);
        if (existingOrderOpt.isPresent()) {
            RelatedProduct existingOrder = existingOrderOpt.get();

            if (!existingOrder.isDeleted()) {
                throw new IllegalStateException("Validation Failed: Display order slot " + displayOrder + " is already occupied by product (variantID: " + existingOrder.getRelatedProductVariant().getId() + ").");
            } else {
                throw new IllegalStateException("Validation Failed: Display order slot " + displayOrder + " is blocked by a deleted relationship record. Clear or purge this slot configuration first.");
            }
        }

        // 6. Build Entity
        RelatedProduct relatedProductEntity = RelatedProduct.builder()
                .product(product)
                .relatedProductVariant(relatedVariant)
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .isDeleted(false)
                .build();

        // 7. Persist to Database
        RelatedProduct saved = relatedProductRepository.save(relatedProductEntity);

        // 8. Return Formatted Data
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeRelatedProduct(Long productId, Long relatedVariantId) {

        RelatedProduct relatedProduct = relatedProductRepository
                .findEvenIfDeleted(productId, relatedVariantId)
                .orElseThrow(() -> new ProductNotFoundException("Related product relationship not found between Product ID: " + productId +
                        " and Variant ID: " + relatedVariantId));

        if (relatedProduct.isDeleted()) {
            throw new IllegalStateException("Validation Failed: This product relationship has already been deleted.");
        }

        relatedProduct.setDeleted(true);
        relatedProduct.setActive(false);
        relatedProductRepository.save(relatedProduct);
    }

    @Override
    @Transactional
    public void restoreRelatedProduct(Long productId, Long relatedVariantId) {

        RelatedProduct relatedProduct = relatedProductRepository
                .findEvenIfDeleted(productId, relatedVariantId)
                .orElseThrow(() -> new ProductNotFoundException("Related product relationship not found between Product ID: " + productId +
                        " and Variant ID: " + relatedVariantId));

        if (!relatedProduct.isDeleted()) {
            throw new IllegalStateException("Validation Failed: This product relationship has already been restored.");
        }

        relatedProduct.setDeleted(false);
        relatedProduct.setActive(false);
        relatedProductRepository.save(relatedProduct);
    }

    @Override
    public List<RelatedProductResponse> getManualRelatedProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // active/inactive only
        List<RelatedProduct> relatedProducts = relatedProductRepository.findAllRelatedProducts(productId);
        return relatedProducts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets related products for customer view
     * Priority: Manual relations first, then auto-suggestions
     * Auto-suggestions based on same category and brand
     * @param productId - ID of the product
     * @return List of up to 6 related products
     */
    @Override
    public List<ProductListResponseDTO> getAllRelatedProducts(Long productId) {
        // Check for manual related products first
        List<RelatedProduct> manualRelated = relatedProductRepository.findActiveRelatedProducts(productId);

        // No manual relations, use auto-suggestion algorithm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Find products with same category/brand (relevance scoring in repository)
        List<Product> autoSuggested = productRepository.findRelatedProducts(
                productId,
                product.getCategory(),
                product.getBrand()
        );

        if (autoSuggested.isEmpty()) {
            return new ArrayList<>();
        }

        // Return maximum 6 auto-suggested products
        return productMapper.toListDTOs(autoSuggested.stream()
                .limit(6)
                .toList());
    }

    @Override
    @Transactional
    public RelatedProductResponse activateRelatedProduct(Long productId, Long relatedVariantId) {

        // 1. Verify a non-deleted relationship exists using your specified function
        boolean exists = relatedProductRepository.existsByProductIdAndRelatedProductVariantIdAndIsDeletedFalse(productId, relatedVariantId);

        if (!exists) {
            throw new ProductNotFoundException(
                    "Cannot activate. No active relationship record found between Product ID: " + productId +
                            " and Variant ID: " + relatedVariantId);
        }

        // 2. Fetch the entity to update its state
        // (Safe to extract via findEvenIfDeleted since step 1 guaranteed its presence)
        RelatedProduct relatedProduct = relatedProductRepository
                .findEvenIfDeleted(productId, relatedVariantId)
                .get();

        // 3. Check if it's already active to prevent redundant database writes
        if (relatedProduct.isActive()) {
            throw new IllegalStateException("Validation Failed: This product relationship is already activated.");
        }

        // 4. Update the visibility status to active
        relatedProduct.setActive(true);

        // 5. Persist and return the updated data configuration
        RelatedProduct updated = relatedProductRepository.save(relatedProduct);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public RelatedProductResponse deactivateRelatedProduct(Long productId, Long relatedVariantId) {

        // 1. Verify a non-deleted relationship exists using your specified function
        boolean exists = relatedProductRepository.existsByProductIdAndRelatedProductVariantIdAndIsDeletedFalse(productId, relatedVariantId);

        if (!exists) {
            throw new ProductNotFoundException(
                    "Cannot activate. No active relationship record found between Product ID: " + productId +
                            " and Variant ID: " + relatedVariantId);
        }

        // 2. Fetch the entity to update its state
        // (Safe to extract via findEvenIfDeleted since step 1 guaranteed its presence)
        RelatedProduct relatedProduct = relatedProductRepository
                .findEvenIfDeleted(productId, relatedVariantId)
                .get();

        // 3. Check if it's already active to prevent redundant database writes
        if (!relatedProduct.isActive()) {
            throw new IllegalStateException("Validation Failed: This product relationship is already deactivated.");
        }

        // 4. Update the visibility status to active
        relatedProduct.setActive(false);

        // 5. Persist and return the updated data configuration
        RelatedProduct updated = relatedProductRepository.save(relatedProduct);
        return mapToResponse(updated);
    }

    private RelatedProductResponse mapToResponse(RelatedProduct relatedProduct) {
        return RelatedProductResponse.builder()
                .id(relatedProduct.getId())
                .productId(relatedProduct.getProduct().getId())
                .productName(relatedProduct.getProduct().getName())
                .relatedProductVariantId(relatedProduct.getRelatedProductVariant().getId())
                .relatedVariantName(relatedProduct.getRelatedProductVariant().getVariantName())
                .relatedProductColor(relatedProduct.getRelatedProductVariant().getColor())
                .relatedProductVariantSku(relatedProduct.getRelatedProductVariant().getSkuCode())
                .displayOrder(relatedProduct.getDisplayOrder())
                .isActive(relatedProduct.isActive())
//                .createdAt(relatedProduct.getCreatedAt())
                .build();
    }
}
