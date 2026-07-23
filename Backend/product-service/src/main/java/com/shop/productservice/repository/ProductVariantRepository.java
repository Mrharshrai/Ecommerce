package com.shop.productservice.repository;

import com.shop.productservice.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // ----------------- COLOR UNIQUE CHECKS PER PRODUCT-----------------
    boolean existsByProductIdAndColorIgnoreCaseAndIsDeletedFalse(Long productId,String color);
    @Query(value = """
        SELECT *
        FROM product_variants
        WHERE product_id = :productId
          AND LOWER(color) = LOWER(:color)
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProductVariant> findColorConflict(
            @Param("productId") Long productId,
            @Param("color") String color
    );

    // ----------------- LOOKUP -----------------
    Optional<ProductVariant> findBySkuCode(String skuCode);

    // ----------------- PRODUCT RELATION -----------------
    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdOrderByCreatedAtDesc(Long productId);

    // ----------------- NAME FILTERING -----------------
    List<ProductVariant> findByVariantNameContainingIgnoreCase(String variantName);

    // ----------------- COLOR FILTERING -----------------
    List<ProductVariant> findByColorIgnoreCase(String color);

    @Query(
            value = "SELECT * FROM product_variants WHERE id = :id",
            nativeQuery = true
    )
    Optional<ProductVariant> findByIdIncludeDeleted(@Param("id") Long id);

    @Query(
            value = "SELECT * FROM product_variants WHERE is_deleted = true ORDER BY updated_at DESC",
            countQuery = "SELECT COUNT(*) FROM product_variants WHERE is_deleted = true",
            nativeQuery = true
    )
    Page<ProductVariant> findAllDeleted(Pageable pageable);

    /**
     * Returns all variants of a product including deleted ones.
     */
    @Query(
            value = "SELECT * FROM product_variants WHERE product_id = :productId",
            nativeQuery = true
    )
    List<ProductVariant> findByProductIdIncludeDeleted(@Param("productId") Long productId);

    @Query(
            value = """
                SELECT *
                FROM product_variants
                WHERE product_id = :productId
                  AND is_deleted = true
                ORDER BY updated_at DESC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM product_variants
                WHERE product_id = :productId
                  AND is_deleted = true
                """,
            nativeQuery = true
    )
    Page<ProductVariant> findDeletedVariantsByProductId(
            @Param("productId") Long productId,
            Pageable pageable
    );


}
