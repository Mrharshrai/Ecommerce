package com.shop.productservice.repository;

import com.shop.productservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndIsDeletedFalse(Long productId);

    Page<Review> findByProductIdAndIsDeletedFalse(Long productId, Pageable pageable);

    List<Review> findByUserEmailAndIsDeletedFalse(String userEmail);

    boolean existsByUserEmailAndProductIdAndIsDeletedFalse(String userEmail, Long productId);

    Optional<Review> findByUserEmailAndProductIdAndOrderIdAndIsDeletedFalse(
            String userEmail, Long productId, Long orderId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isDeleted = false")
    Long countByProductId(@Param("productId") Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isDeleted = false")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId " +
           "AND r.isDeleted = false GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);
}
