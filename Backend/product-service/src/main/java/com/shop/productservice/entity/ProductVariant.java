package com.shop.productservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_skuCode", columnList = "skuCode", unique = true)
})
@SQLDelete(sql = "UPDATE product_variants SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "SKU code cannot be empty")
    @Size(max = 100, message = "SKU code must be less than 100 characters")
    @Column(nullable = false, unique = true)
    private String skuCode;

    @NotBlank(message = "Variant name cannot be empty")
    @Size(max = 255)
    @Column(nullable = false)
    private String variantName;

    @NotBlank(message = "Color cannot be empty")
    @Size(max = 100)
    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalProductVariantQuantity = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariantSize> sizes = new ArrayList<>();

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariantImage> images = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false,columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

    // ---- HELPERS ----

    @PrePersist
    @PreUpdate
    private void recalcBeforeSave() {
        recalculateTotalProductVariantQuantity();
    }

    public void recalculateTotalProductVariantQuantity() {
        if (sizes == null || sizes.isEmpty()) {
            this.totalProductVariantQuantity = 0;
        } else {
            // Count ONLY active + not deleted sizes
            this.totalProductVariantQuantity = sizes.stream()
                    .filter(s -> s.isActive() && !s.isDeleted())
                    .mapToInt(s -> s.getQuantity() == null ? 0 : s.getQuantity())
                    .sum();
        }
    }

}
