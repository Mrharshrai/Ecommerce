package com.shop.userservice.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @NotBlank(message = "Address line 1 is required")
    @Column(nullable = false)
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    @Column(nullable = false, length = 6)
    private String pincode;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    // Use columnDefinition = "TIMESTAMP(6)" for microsecond precision in MySQL
    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;
}
