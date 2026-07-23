package com.shop.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "warehouse_inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "variant_size_id"})
}, indexes = {
        @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_inventory_variant_size", columnList = "variant_size_id")
})
@SQLDelete(sql = "UPDATE warehouse_inventory SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WarehouseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Warehouse is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull(message = "Product variant size is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_size_id", nullable = false)
    private ProductVariantSize variantSize;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    @NotNull(message = "Reserved quantity is required")
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    @Builder.Default
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

    public Integer getAvailableQuantity() {
        return Math.max(0, quantity - reservedQuantity);
    }

//    public Integer getAvailableQuantity() {
//        return Math.max(0, quantity - (reservedQuantity == null ? 0 : reservedQuantity));
//    }
}
