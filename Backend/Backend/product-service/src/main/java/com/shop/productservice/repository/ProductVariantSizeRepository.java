package com.shop.productservice.repository;

import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.enums.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductVariantSizeRepository extends JpaRepository<ProductVariantSize, Long> {

    // ----------------- LOOKUP -----------------
    Optional<ProductVariantSize> findBySizeSku(String sizeSku);

    Optional<ProductVariantSize> findByVariant_SkuCodeAndSize(String variantSkuCode, Size size);

    // ----------------- VARIANT RELATION -----------------
    List<ProductVariantSize> findByVariant_IdOrderByCreatedAtDesc(Long variantId);

    // ----------------- SIZE FILTERING -----------------
    List<ProductVariantSize> findBySize(Size size);


    // ----------------- PRODUCT RELATION THROUGH ASIN -----------------
    @Query(
            value = "SELECT * FROM product_variant_sizes WHERE id = :id",
            nativeQuery = true
    )
    Optional<ProductVariantSize> findByIdIncludeDeleted(@Param("id") Long id);

    @Query(
            value = "SELECT * FROM product_variant_sizes WHERE is_deleted = true ORDER BY updated_at DESC",
            countQuery = "SELECT COUNT(*) FROM product_variant_sizes WHERE is_deleted = true",
            nativeQuery = true
    )
    Page<ProductVariantSize> findAllDeleted(Pageable pageable);

    /**
     * =============================================================
     * Returns all deleted sizes belonging to a Variant.
     * Parent Variant can be:
     * • Active
     * • Inactive
     * • Deleted
     * Ordered by latest deleted first.
     * =============================================================
     */
    @Query(
            value = """
                SELECT *
                FROM product_variant_sizes
                WHERE variant_id = :variantId
                  AND is_deleted = true
                ORDER BY updated_at DESC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM product_variant_sizes
                WHERE variant_id = :variantId
                  AND is_deleted = true
                """,
            nativeQuery = true
    )
    Page<ProductVariantSize> findDeletedSizesByVariantId(
            @Param("variantId") Long variantId,
            Pageable pageable
    );

    boolean existsByVariant_IdAndSizeAndIsDeletedFalse(Long variantId, Size size);

    /**
     * Returns all sizes of a variant including deleted ones.
     */
    @Query(
            value = "SELECT * FROM product_variant_sizes WHERE variant_id = :variantId",
            nativeQuery = true
    )
    List<ProductVariantSize> findByVariantIdIncludeDeleted(@Param("variantId") Long variantId);

    @Query(
            value = """
        SELECT variant_id
        FROM product_variant_sizes
        WHERE id = :sizeId
        """,
            nativeQuery = true
    )
    Long findVariantIdBySizeId(@Param("sizeId") Long sizeId);

    @Query(value = """
        SELECT *
        FROM product_variant_sizes
        WHERE variant_id = :variantId
          AND size = :size
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProductVariantSize> findSizeConflict(
            @Param("variantId") Long variantId,
            @Param("size") String size
    );
}
