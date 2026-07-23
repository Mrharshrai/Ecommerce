package com.shop.productservice.repository;

import com.shop.productservice.entity.RelatedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelatedProductRepository extends JpaRepository<RelatedProduct, Long> {

    Optional<RelatedProduct> findByProduct_IdAndRelatedProductVariant_Id(Long productId, Long relatedVariantId);

    @Query("SELECT rp FROM RelatedProduct rp " +
            "WHERE rp.product.id = :productId " +
            "AND rp.isActive = true " +
            "AND rp.isDeleted = false " +
            "ORDER BY rp.displayOrder ASC, rp.createdAt DESC")
    List<RelatedProduct> findActiveRelatedProducts(@Param("productId") Long productId);

    // Optimized JPQL query to load look items sorted from smallest order to greatest order
    @Query("SELECT rp FROM RelatedProduct rp " +
            "JOIN FETCH rp.relatedProductVariant v " +
            "JOIN FETCH v.product " +
            "WHERE rp.product.id = :productId " +
            "AND rp.isDeleted = false " +
            "ORDER BY rp.displayOrder ASC, rp.createdAt DESC")
    List<RelatedProduct> findAllRelatedProducts(@Param("productId") Long productId);

//    boolean existsByProductIdAndRelatedProductIdAndIsDeletedFalse(Long productId, Long relatedProductId);

    // Check if the display order is already occupied for this main product
    boolean existsByProductIdAndDisplayOrderAndIsDeletedFalse(Long productId, Integer displayOrder);

//    Optional<RelatedProduct> findByProductIdAndRelatedProductIdAndIsDeletedFalse(Long productId, Long relatedProductId);

    // Check if this specific variant is already linked to the main product page
    boolean existsByProductIdAndRelatedProductVariantIdAndIsDeletedFalse(Long productId, Long relatedProductVariantId);

    @Query(value = "SELECT * FROM related_products WHERE product_id = :productId AND related_variant_id = :variantId LIMIT 1", nativeQuery = true)
    Optional<RelatedProduct> findEvenIfDeleted(@Param("productId") Long productId, @Param("variantId") Long variantId);

    // NEW: Native query to look up any relationship occupying this display order slot (active or deleted)
    @Query(value = "SELECT * FROM related_products WHERE product_id = :productId AND display_order = :displayOrder LIMIT 1", nativeQuery = true)
    Optional<RelatedProduct> findDisplayOrderEvenIfDeleted(@Param("productId") Long productId, @Param("displayOrder") Integer displayOrder);

}
