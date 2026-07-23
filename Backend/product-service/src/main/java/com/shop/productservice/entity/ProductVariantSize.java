package com.shop.productservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.shop.productservice.enums.Size;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variant_sizes", indexes = {
        @Index(name = "idx_size_sku", columnList = "sizeSku", unique = true)
})
@SQLDelete(sql = "UPDATE product_variant_sizes SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductVariantSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @jakarta.validation.constraints.Size(max = 100)
    @NotBlank(message = "sizeSku is required")
    @Column(unique = true, nullable = false)
    private String sizeSku; // Auto-generated via variant + color + size

    @Column(name = "size", nullable = false, length = 20)
    @NotNull(message = "size is required")
    @Enumerated(EnumType.STRING)
    private Size size; // e.g., S, M, L, XL

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    @NotNull(message = "MRP cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mrp; // Selling price / discounts will come from separate Pricing Service in future

    @NotNull(message = "Weight cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @NotNull(message = "Length cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Length must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal length;

    @NotNull(message = "Width cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Width must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal width;

    @NotNull(message = "Height cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal height;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @OneToMany(mappedBy = "variantSize", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WarehouseInventory> warehouseInventories = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

    public Integer getTotalWarehouseQuantity() {
        if (warehouseInventories == null || warehouseInventories.isEmpty()) {
            return 0;
        }
        return warehouseInventories.stream()
                .filter(wi -> !wi.isDeleted())
                .mapToInt(wi -> wi.getQuantity() == null ? 0 : wi.getQuantity())
                .sum();
    }

    public Integer getTotalAvailableQuantity() {
        if (warehouseInventories == null || warehouseInventories.isEmpty()) {
            return 0;
        }
        return warehouseInventories.stream()
                .filter(wi -> !wi.isDeleted() && wi.isActive())
                .mapToInt(WarehouseInventory::getAvailableQuantity)
                .sum();
    }

    public Integer getTotalReservedQuantity() {
        if (warehouseInventories == null || warehouseInventories.isEmpty()) {
            return 0;
        }
        return warehouseInventories.stream()
                .filter(wi -> !wi.isDeleted())
                .mapToInt(wi -> wi.getReservedQuantity() == null ? 0 : wi.getReservedQuantity())
                .sum();
    }

}