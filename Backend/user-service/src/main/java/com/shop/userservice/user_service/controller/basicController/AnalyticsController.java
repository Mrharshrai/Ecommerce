package com.shop.userservice.user_service.controller.basicController;

import com.shop.userservice.user_service.entity.UserActivityLog;
import com.shop.userservice.user_service.service.UserActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {
    
    private final UserActivityService activityService;
    
    public AnalyticsController(UserActivityService activityService) {
        this.activityService = activityService;
    }
    
    /**
     * Get activity history for a specific user
     */
    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<Page<UserActivityLog>> getUserActivityHistory(
            @PathVariable("userId") Long userId,
            Pageable pageable) {
        Page<UserActivityLog> activities = activityService.getUserActivityHistory(userId, pageable);
        return ResponseEntity.ok(activities);
    }
    
    /**
     * Get last login time for a user
     */
    @GetMapping("/users/{userId}/last-login")
    public ResponseEntity<Map<String, Object>> getLastLoginTime(@PathVariable("userId") Long userId) {
        Instant lastLogin = activityService.findLastLoginTime(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("lastLoginTime", lastLogin);
        response.put("hasLoggedIn", lastLogin != null);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Count recent logins for a user (suspicious activity detection)
     */
    @GetMapping("/users/{userId}/login-frequency")
    public ResponseEntity<Map<String, Object>> getLoginFrequency(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "1") int hours) {
        
        long loginCount = activityService.countRecentLogins(userId, hours);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("loginCount", loginCount);
        response.put("timeWindow", hours + " hour(s)");
        response.put("suspicious", loginCount > 5); // Flag if more than 5 logins in time window
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get system-wide statistics
     */
    @GetMapping("/system/statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = activityService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get recent activities across all users
     */
    @GetMapping("/activities/recent")
    public ResponseEntity<Page<UserActivityLog>> getRecentActivities(Pageable pageable) {
        Page<UserActivityLog> activities = activityService.getRecentActivities(pageable);
        return ResponseEntity.ok(activities);
    }
    
    /**
     * Get admin activities
     */
    @GetMapping("/activities/admin")
    public ResponseEntity<Page<UserActivityLog>> getAdminActivities(Pageable pageable) {
        Page<UserActivityLog> activities = activityService.getAdminActivities(pageable);
        return ResponseEntity.ok(activities);
    }
}
