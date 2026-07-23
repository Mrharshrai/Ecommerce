package com.shop.productservice.entity;

import com.shop.productservice.enums.DiscountApplyTo;
import com.shop.productservice.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts", indexes = {@Index(name = "idx_discount_code", columnList = "discount_code", unique = true), @Index(name = "idx_discount_product", columnList = "product_id"), @Index(name = "idx_discount_category", columnList = "category"), @Index(name = "idx_discount_dates", columnList = "start_date, end_date")})
@SQLDelete(sql = "UPDATE discounts SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "discount_code", unique = true, nullable = false)
    @NotBlank(message = "Discount code cannot be empty")
    @Size(max = 50, message = "Discount code must be less than 50 characters")
    private String discountCode;

    @Column(name = "discount_name", nullable = false)
    @NotBlank(message = "Discount name cannot be empty")
    @Size(max = 255, message = "Discount name must be less than 255 characters")
    private String discountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    @Column(name = "min_product_price", precision = 10, scale = 2)
    private BigDecimal minProductPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_to", nullable = false)
    @NotNull(message = "Apply to is required")
    private DiscountApplyTo applyTo;

    @Column(name = "category")
    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private Instant startDate;


    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private Instant endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false,columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

    @Column(name = "max_usage_count")
    @Min(value = 1, message = "Max usage count must be at least 1")
    @Max(value = 999999, message = "Max usage count cannot exceed 999,999")
    private Integer maxUsageCount; // null = unlimited

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private int usedCount = 0;
    // ADD to entity — Hibernate will add a "version" column and auto-increment it
    // If two transactions write simultaneously, one gets OptimisticLockException
    @Version
    private Long version;

    // Method to atomically check and increment — call this at order time
    public boolean tryRedeem() {
        if (maxUsageCount != null && usedCount >= maxUsageCount) {
            return false; // quota exhausted
        }
        usedCount++;
        return true;
    }

    /**
     * Logic updated to use Instant.
     * Usage: discount.isCurrentlyValid(Instant.now());
     */
    public boolean isCurrentlyValid(Instant referenceTime) {
        boolean withinQuota = maxUsageCount == null || usedCount < maxUsageCount;

        return isActive && !isDeleted
                && !referenceTime.isBefore(startDate)
                && !referenceTime.isAfter(endDate)
                && withinQuota;
    }
}
