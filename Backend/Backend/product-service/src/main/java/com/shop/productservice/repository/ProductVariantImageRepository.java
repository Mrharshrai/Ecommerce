package com.shop.productservice.repository;


import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.entity.ProductVariantSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, Long> {
    // ----------------- VARIANT RELATION -----------------
    List<ProductVariantImage> findByVariant_IdOrderByCreatedAtDesc(Long variantId);

    List<ProductVariantImage> findByVariantIdAndIsActiveTrueOrderBySortOrderAsc(Long variantId);

    // ----------------- PRODUCT RELATION THROUGH ASIN -----------------

    @Query(
            value = "SELECT * FROM product_variant_images WHERE id = :id",
            nativeQuery = true
    )
    Optional<ProductVariantImage> findByIdIncludeDeleted(@Param("id") Long id);

    @Query(
            value = "SELECT * FROM product_variant_images WHERE is_deleted = true ORDER BY updated_at DESC",
            countQuery = "SELECT COUNT(*) FROM product_variant_images WHERE is_deleted = true",
            nativeQuery = true
    )
    Page<ProductVariantImage> findAllDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM product_variant_images WHERE variant_id = :variantId AND (sort_order = :sortOrder OR image = :url)", nativeQuery = true)
    Optional<ProductVariantImage> findAnyConflict(@Param("variantId") Long variantId, @Param("sortOrder") Integer sortOrder, @Param("url") String url);

    @Query(value = "SELECT * FROM product_variant_images " +
            "WHERE variant_id = :variantId " +
            "AND id <> :currentId " + // <--- EXCLUDE SELF
            "AND (sort_order = :sortOrder OR image = :url) " +
            "LIMIT 1", nativeQuery = true)
    Optional<ProductVariantImage> findConflictForUpdate(
            @Param("variantId") Long variantId,
            @Param("currentId") Long currentId,
            @Param("sortOrder") Integer sortOrder,
            @Param("url") String url
    );

    /**
     * Returns all images of a variant including deleted ones.
     */
    @Query(
            value = "SELECT * FROM product_variant_images WHERE variant_id = :variantId ORDER BY sort_order ASC",
            nativeQuery = true
    )
    List<ProductVariantImage> findByVariantIdIncludeDeleted(@Param("variantId") Long variantId);

    @Query(
            value = """
                    SELECT variant_id
                    FROM product_variant_images
                    WHERE id = :imageId
                    """,
            nativeQuery = true
    )
    Long findVariantIdByImageId(@Param("imageId") Long imageId);

    @Query(value = """
            SELECT *
            FROM product_variant_images
            WHERE variant_id = :variantId
            AND image = :image
            AND sort_order = :sortOrder
            LIMIT 1
            """, nativeQuery = true)
    Optional<ProductVariantImage> findExactConflict(
            @Param("variantId") Long variantId,
            @Param("image") String image,
            @Param("sortOrder") Integer sortOrder
    );

    @Query(value = """
            SELECT *
            FROM product_variant_images
            WHERE variant_id = :variantId
            AND image = :image
            LIMIT 1
            """, nativeQuery = true)
    Optional<ProductVariantImage> findImageUrlConflict(
            @Param("variantId") Long variantId,
            @Param("image") String image
    );

    @Query(value = """
            SELECT *
            FROM product_variant_images
            WHERE variant_id = :variantId
            AND sort_order = :sortOrder
            LIMIT 1
            """, nativeQuery = true)
    Optional<ProductVariantImage> findSortOrderConflict(
            @Param("variantId") Long variantId,
            @Param("sortOrder") Integer sortOrder
    );

    @Query(value = """
        SELECT *
        FROM product_variant_images
        WHERE variant_id = :variantId
          AND id <> :currentId
          AND image = :image
          AND sort_order = :sortOrder
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProductVariantImage> findExactConflictForUpdate(
            @Param("variantId") Long variantId,
            @Param("currentId") Long currentId,
            @Param("image") String image,
            @Param("sortOrder") Integer sortOrder
    );

    @Query(value = """
        SELECT *
        FROM product_variant_images
        WHERE variant_id = :variantId
          AND id <> :currentId
          AND image = :image
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProductVariantImage> findImageUrlConflictForUpdate(
            @Param("variantId") Long variantId,
            @Param("currentId") Long currentId,
            @Param("image") String image
    );

    @Query(value = """
        SELECT *
        FROM product_variant_images
        WHERE variant_id = :variantId
          AND id <> :currentId
          AND sort_order = :sortOrder
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProductVariantImage> findSortOrderConflictForUpdate(
            @Param("variantId") Long variantId,
            @Param("currentId") Long currentId,
            @Param("sortOrder") Integer sortOrder
    );

    /**
     * =============================================================
     * Returns all deleted images belonging to a Variant.
     *
     * Parent Variant can be:
     * • Active
     * • Inactive
     * • Deleted
     *
     * Ordered by latest deleted first.
     * =============================================================
     */
    @Query(
            value = """
                SELECT *
                FROM product_variant_images
                WHERE variant_id = :variantId
                  AND is_deleted = true
                ORDER BY updated_at DESC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM product_variant_images
                WHERE variant_id = :variantId
                  AND is_deleted = true
                """,
            nativeQuery = true
    )
    Page<ProductVariantImage> findDeletedImagesByVariantId(
            @Param("variantId") Long variantId,
            Pageable pageable
    );


}

