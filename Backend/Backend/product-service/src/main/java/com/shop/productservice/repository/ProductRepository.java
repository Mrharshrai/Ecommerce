package com.shop.productservice.repository;


import com.shop.productservice.entity.Product;
import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ----------------- LOOKUP -----------------
    Optional<Product> findByAsin(String asin);

    boolean existsByAsinAndIsDeletedFalse(String asin);

    Optional<Product> findByAsinAndIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(String asin);

    List<Product> findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse();

    Page<Product> findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalse(Pageable pageable);

    Page<Product> findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalseOrderByPublishedAtDesc(Pageable pageable);

    List<Product> findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalseAndCategoryContainingIgnoreCaseAndSubCategoryContainingIgnoreCaseOrderByPublishedAtDesc(
            String category, String subCategory
    );

    Page<Product> findByIsPublishedTrueAndIsActiveTrueAndIsDeletedFalseAndCategoryContainingIgnoreCaseAndSubCategoryContainingIgnoreCaseOrderByPublishedAtDesc(
            Pageable pageable, String category, String subCategory
    );

    @Query("""
            SELECT DISTINCT p
            FROM Product p
            JOIN p.variants v
            JOIN v.sizes s
            JOIN v.images i
            WHERE
                p.isPublished = false
                AND p.isActive = true
                AND p.isDeleted = false
            
                AND v.isActive = true
                AND v.isDeleted = false
            
                AND s.isActive = true
                AND s.isDeleted = false
                AND s.quantity > 0
            
                AND i.isActive = true
                AND i.isDeleted = false
            
            ORDER BY p.createdAt DESC
            """)
    Page<Product> findAllReadyToPublishProducts(Pageable pageable);


    @Query("SELECT p FROM Product p JOIN p.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Product> searchByTag(String tag);


    // ----------------- ACTIVE / NOT DELETED -----------------


    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    List<Product> findAllNotDeleted();

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findActiveById(Long id);

    @Query("""
                SELECT p FROM Product p 
                JOIN p.tags t 
                WHERE p.isPublished = true 
                  AND p.isActive = true 
                  AND p.isDeleted = false
                  AND (:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%')))
                  AND (:subCategory IS NULL OR LOWER(p.subCategory) LIKE LOWER(CONCAT('%', :subCategory, '%')))
                  AND (:gender IS NULL OR p.gender = :gender)
                  AND (:tag IS NULL OR LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%')))
                  ORDER BY p.publishedAt DESC
            """)
    List<Product> filterProducts(
            @Param("category") String category,
            @Param("subCategory") String subCategory,
            @Param("gender") Gender gender,
            @Param("tag") String tag
    );

    @Query("""
                SELECT DISTINCT p FROM Product p
                JOIN p.tags t
                WHERE p.isPublished = true
                  AND p.isActive = true
                  AND p.isDeleted = false
                  AND (:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%')))
                  AND (:subCategory IS NULL OR LOWER(p.subCategory) LIKE LOWER(CONCAT('%', :subCategory, '%')))
                  AND (:gender IS NULL OR p.gender = :gender)
                  AND (:tag IS NULL OR LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%')))
                  ORDER BY p.publishedAt DESC
            """)
    Page<Product> filterProductsPageable(
            Pageable pageable,
            @Param("category") String category,
            @Param("subCategory") String subCategory,
            @Param("gender") Gender gender,
            @Param("tag") String tag
    );

    @Query("""
                SELECT DISTINCT p FROM Product p
                JOIN p.variants v
                JOIN v.sizes s
                WHERE p.isPublished = true
                  AND p.isActive = true
                  AND p.isDeleted = false
                  AND v.isActive = true
                  AND v.isDeleted = false
                  AND s.isActive = true
                  AND s.isDeleted = false
                  AND s.mrp BETWEEN :minPrice AND :maxPrice
                  ORDER BY p.publishedAt DESC
            """)
    List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("""
                SELECT DISTINCT p FROM Product p
                JOIN p.variants v
                JOIN v.sizes s
                WHERE p.isPublished = true
                  AND p.isActive = true
                  AND p.isDeleted = false
                  AND v.isActive = true
                  AND v.isDeleted = false
                  AND s.isActive = true
                  AND s.isDeleted = false
                  AND s.mrp BETWEEN :minPrice AND :maxPrice
                  ORDER BY p.publishedAt DESC
            """)
    Page<Product> findProductsByPriceRangePageable(Pageable pageable, BigDecimal minPrice, BigDecimal maxPrice);

    @Query(
            value = "SELECT * FROM products WHERE id = :id",
            nativeQuery = true
    )
    Optional<Product> findByIdIncludeDeleted(@Param("id") Long id);

    @Query(
            value = """
                        SELECT *
                        FROM products
                        WHERE is_deleted = true
                        ORDER BY updated_at DESC
                    """,
            countQuery = """
                        SELECT COUNT(*)
                        FROM products
                        WHERE is_deleted = true
                    """,
            nativeQuery = true
    )
    Page<Product> findAllDeleted(Pageable pageable);

    Page<Product> findByIsDeletedFalseOrderByUpdatedAtDesc(Pageable pageable);

    Page<Product> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);


    @Query("""
                SELECT p FROM Product p
                WHERE p.id != :productId
                  AND p.isPublished = true
                  AND p.isActive = true
                  AND p.isDeleted = false
                  AND (p.category = :category OR p.brand = :brand)
                ORDER BY 
                  CASE WHEN p.category = :category AND p.brand = :brand THEN 1
                       WHEN p.category = :category THEN 2
                       WHEN p.brand = :brand THEN 3
                       ELSE 4 END,
                  p.publishedAt DESC
            """)
    List<Product> findRelatedProducts(
            @Param("productId") Long productId,
            @Param("category") String category,
            @Param("brand") String brand
    );

}

