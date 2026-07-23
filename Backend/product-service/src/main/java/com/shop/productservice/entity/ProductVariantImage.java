package com.shop.productservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "product_variant_images", indexes = {
        @Index(name = "idx_variant_image_variant_id", columnList = "variant_id")
})
@SQLDelete(sql = "UPDATE product_variant_images SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductVariantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    @Column(nullable = false, length = 500)
    private String image;

    /**
     * Order of display:
     * 1 = primary image
     * 2 = Secondary image
     * 3 = Close-up / details
     */
    @NotNull(message = "Sort order is required")
    @Min(value = 1, message = "Sort order must be greater than or equal to 1")
    @Column(nullable = false)
    private Integer sortOrder;

    @Size(max = 255, message = "Alt text must be less than 255 characters")
    private String altText;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

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
    @Column(nullable = false,columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

}
