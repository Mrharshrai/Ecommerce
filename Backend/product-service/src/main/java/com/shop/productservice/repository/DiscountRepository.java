package com.shop.productservice.repository;

import com.shop.productservice.entity.Discount;
import com.shop.productservice.enums.DiscountApplyTo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByDiscountCodeAndIsDeletedFalse(String discountCode);

    boolean existsByDiscountCodeAndIsDeletedFalse(String discountCode);

    // 2. The "Global" check (Sees everything: Active AND Deleted)
    @Query(value = "SELECT * FROM discounts WHERE UPPER(discount_code) = UPPER(:code) LIMIT 1", nativeQuery = true)
    Discount findByCodeIgnoreDeletedFilter(@Param("code") String code);

    List<Discount> findByIsActiveTrueAndIsDeletedFalse();

    @Query("SELECT d FROM Discount d WHERE d.product.id = :productId " +
           "AND d.applyTo = 'PRODUCT' " +
           "AND d.isActive = true " +
           "AND d.isDeleted = false " +
           "AND :now BETWEEN d.startDate AND d.endDate " +  //now >= startDate && now <= endDate
           "ORDER BY d.createdAt DESC")
    List<Discount> findActiveProductDiscounts(@Param("productId") Long productId,
                                              @Param("now") Instant now);

    @Query("SELECT d FROM Discount d WHERE d.category = :category " +
           "AND d.applyTo = 'CATEGORY' " +
           "AND d.isActive = true " +
           "AND d.isDeleted = false " +
           "AND :now BETWEEN d.startDate AND d.endDate " +
           "ORDER BY d.createdAt DESC")
    List<Discount> findActiveCategoryDiscounts(@Param("category") String category, 
                                               @Param("now") Instant now);

    @Query("SELECT COUNT(d) > 0 FROM Discount d WHERE d.product.id = :productId " +
           "AND d.isActive = true " +
           "AND d.isDeleted = false " +
           "AND :now BETWEEN d.startDate AND d.endDate")
    boolean existsActiveProductDiscount(@Param("productId") Long productId,
                                        @Param("now") Instant now);

    @Query("SELECT COUNT(d) > 0 FROM Discount d WHERE d.category = :category " +
           "AND d.isActive = true " +
           "AND d.isDeleted = false " +
           "AND :now BETWEEN d.startDate AND d.endDate")
    boolean existsActiveCategoryDiscount(@Param("category") String category,
                                         @Param("now") Instant now);

    List<Discount> findByApplyToAndIsDeletedFalse(DiscountApplyTo applyTo);

    @Query("SELECT d FROM Discount d WHERE d.isDeleted = true")
    List<Discount> findAllDeleted();
}
