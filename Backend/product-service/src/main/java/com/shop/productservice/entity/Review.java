package com.shop.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_reviews", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_user", columnList = "user_email"),
        @Index(name = "idx_review_rating", columnList = "rating")
}, uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_product_order", columnNames = {"user_email", "product_id",
                "order_id"})
})
@SQLDelete(sql = "UPDATE product_reviews SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "user_email", nullable = false)
    @NotNull(message = "User email is required")
    private String userEmail;

    @Column(name = "order_id", nullable = false)
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @Column(name = "rating", nullable = false)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Column(name = "review_title")
    @Size(max = 255, message = "Review title must be less than 255 characters")
    private String reviewTitle;

    @Column(name = "review_text", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Review text must be less than 2000 characters")
    private String reviewText;

    // Stores up to 4 image URLs/data-URIs attached to this review
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_images",
            joinColumns = @JoinColumn(name = "review_id")
    )
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    @Builder.Default
    private List<String> reviewImages = new ArrayList<>();

    @Column(name = "is_verified_purchase", nullable = false)
    @Builder.Default
    private boolean isVerifiedPurchase = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;
}
