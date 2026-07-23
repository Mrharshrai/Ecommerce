package com.shop.userservice.user_service.repository;

import com.shop.userservice.user_service.entity.ActivityType;
import com.shop.userservice.user_service.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    
    // Get all activities for a user (paginated)
    Page<UserActivityLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    // Get activities by type for a user
    Page<UserActivityLog> findByUserIdAndActivityTypeOrderByTimestampDesc(
            Long userId, ActivityType activityType, Pageable pageable);
    
    // Count logins in last hour for a user
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.userId = :userId " +
            "AND l.activityType = 'LOGIN' AND l.timestamp >= :since")
    long countLoginsSince(@Param("userId") Long userId, @Param("since") Instant since);

    // Get last login timestamp for a user
    // Updated: Get last login Instant
    @Query("SELECT MAX(l.timestamp) FROM UserActivityLog l WHERE l.userId = :userId " +
            "AND l.activityType = 'LOGIN'")
    Instant findLastLoginTime(@Param("userId") Long userId);
    
    // Count total logins
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'LOGIN'")
    long countTotalLogins();
    
    // Count total registrations
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'REGISTRATION'")
    long countTotalRegistrations();
    
    // Count activities by type in date range
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.activityType = :activityType " +
            "AND l.timestamp BETWEEN :startDate AND :endDate")
    long countByActivityTypeAndDateRange(
            @Param("activityType") ActivityType activityType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
    
    // Get recent activities (all users)
    @Query("SELECT l FROM UserActivityLog l ORDER BY l.timestamp DESC")
    Page<UserActivityLog> findRecentActivities(Pageable pageable);

    // Get admin activities
    Page<UserActivityLog> findByUserRoleOrderByTimestampDesc(String userRole, Pageable pageable);
}
