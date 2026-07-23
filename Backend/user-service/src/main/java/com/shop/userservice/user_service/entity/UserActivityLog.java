package com.shop.userservice.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(nullable = false, length = 50)
    private String userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

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
