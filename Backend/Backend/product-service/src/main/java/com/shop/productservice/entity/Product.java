package com.shop.productservice.entity;

import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_product_asin", columnList = "asin", unique = true),
                @Index(name = "idx_product_category", columnList = "category"),
                @Index(name = "idx_product_subCategory", columnList = "subCategory"),
                @Index(name = "idx_product_brand", columnList = "brand")
        })
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "asin", unique = true,nullable = false)
    @Size(max = 100, message = "ASIN can be at most 100 characters")
    @NotBlank(message = "asin no. is required")
    private String asin;

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 500, message = "Description must be under 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotBlank(message = "Category cannot be empty")
    @Size(max = 100, message = "Category must be under 100 characters")
    @Column(nullable = false)
    private String category;       // Main category (example: Clothing, Electronics)

    @Size(max = 100, message = "subCategory must be under 100 characters")
    private String subCategory;   // Sub-category (example: Men's T-Shirts, Earbuds)

    @NotBlank(message = "Brand cannot be empty")
    @Size(max = 100, message = "brand must be under 100 characters")
    @Column(nullable = false)
    private String brand;

    @Size(max = 255, message = "material must be under 255 characters")
    @NotBlank(message = "Material is required")
    private String material;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @NotNull(message = "gender is required")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @NotNull(message = "ageGroup is required")
    private AgeGroup ageGroup;
    // Example: "3-5 years", "Adults", "Kids", etc.

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "total_product_quantity", nullable = false)
    @Builder.Default
    private Integer totalProductQuantity = 0;

    // TOTAL quantity across:
    // - All warehouses
    // - All drivers
    // Example:
    // availableQuantity = warehouse stock + driver stock

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;


    // ------------------ SEARCH HELPERS ------------------
    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_highlights", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "highlight", length = 300)
    @Builder.Default
    private List<String> highlights = new ArrayList<>();
    // Bullet point features (shown on UI) Eg. 100% combed cotton, Regular fit, Bio-washed fabric, Suitable for summer, Made in India
    // Keywords for search (e.g., "cotton", "summer", "casual")


    // ------------------ STATUS FLAGS ------------------
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true; // will active once all variants/size/ image added via activateProduct();

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isPublished = false; // when publish only then visible to customer

    @Column(name = "published_at", columnDefinition = "TIMESTAMP(6)")
    private Instant publishedAt;

    // ------------------ TIMESTAMPS ------------------
    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false,columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

    /**
     * Ensure totals are recalculated before saving product via JPA.
     * NOTE: main consistency must be enforced by service methods that change variants.
     */
    @PrePersist
    @PreUpdate
    private void recalcBeforeSave() {
        recalculateTotalProductQuantity();
    }

    public void recalculateTotalProductQuantity() {
        if (variants == null || variants.isEmpty()) {
            this.totalProductQuantity = 0;
        } else {
            // Count ONLY active + not deleted variants
                    this.totalProductQuantity = variants.stream()
                            .filter(v -> v.isActive() && !v.isDeleted())
                            .map(v -> v.getTotalProductVariantQuantity() == null ? 0 : v.getTotalProductVariantQuantity())
                            .reduce(0, (a, b) -> a + b);
        }
    }

    // Convenience helper: call from service when publishing
    public void markPublished(Instant publishedAtTime) {
        this.isPublished = true;
        this.publishedAt = publishedAtTime;
    }

    public void markUnpublished() {
        this.isPublished = false;
        this.publishedAt = null;
    }

    public boolean hasSellableVariant() {
        return variants.stream()
                .filter(v -> v.isActive() && !v.isDeleted())
                .anyMatch(v ->
                        v.getSizes().stream().anyMatch(s ->
                                s.isActive() && !s.isDeleted() && s.getQuantity() > 0
                        )
                                &&
                                v.getImages().stream().anyMatch(img ->
                                        img.isActive() && !img.isDeleted()
                                )
                );
    }


}
