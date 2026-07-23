package com.shop.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_warehouse_code", columnList = "warehouseCode", unique = true),
        @Index(name = "idx_warehouse_city", columnList = "city"),
        @Index(name = "idx_warehouse_state", columnList = "state")
})
@SQLDelete(sql = "UPDATE warehouses SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Warehouse code must be less than 50 characters")
    @Column(nullable = false, unique = true)
    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name must be less than 255 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must be less than 500 characters")
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be less than 100 characters")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must be less than 10 characters")
    @Column(nullable = false)
    private String pincode;

    @Size(max = 100, message = "Country must be less than 100 characters")
    @Builder.Default
    @Column(nullable = false)
    private String country = "India";

    @Size(max = 20, message = "Contact number must be less than 20 characters")
    private String contactNumber;

    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;
}
