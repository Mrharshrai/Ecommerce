package com.shop.productservice.entity;

import com.shop.productservice.enums.ProductActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "product_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(nullable = false, length = 50)
    private String userRole;

    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductActivityType activityType;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant timestamp;
}
