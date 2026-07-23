package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.ActivityType;
import com.shop.userservice.user_service.entity.UserActivityLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

public interface UserActivityService {
    
    /**
     * Log a user activity
     */
    void logActivity(Long userId, String userEmail, String userRole, 
                    ActivityType activityType, String description, 
                    HttpServletRequest request);
    
    /**
     * Log activity with minimal info (no request)
     */
    void logActivity(Long userId, String userEmail, String userRole,
                    ActivityType activityType, String description);
    
    /**
     * Get activity history for a user
     */
    Page<UserActivityLog> getUserActivityHistory(Long userId, Pageable pageable);
    
    /**
     * Get last login time for a user
     */
    Instant findLastLoginTime(Long userId);
    
    /**
     * Count logins in last hour for a user
     */
    long countRecentLogins(Long userId, int hours);
    
    /**
     * Get system-wide statistics
     */
    Map<String, Object> getSystemStatistics();
    
    /**
     * Get recent activities (all users) - Admin only
     */
    Page<UserActivityLog> getRecentActivities(Pageable pageable);
    
    /**
     * Get admin activities - Admin only
     */
    Page<UserActivityLog> getAdminActivities(Pageable pageable);
}
