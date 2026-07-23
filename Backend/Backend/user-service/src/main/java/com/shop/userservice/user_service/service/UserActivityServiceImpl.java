package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.ActivityType;
import com.shop.userservice.user_service.entity.UserActivityLog;
import com.shop.userservice.user_service.exception.ResourceNotFoundException;
import com.shop.userservice.user_service.repository.UserActivityLogRepository;
import com.shop.userservice.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserActivityServiceImpl implements UserActivityService {
    
    private static final Logger log = LoggerFactory.getLogger(UserActivityServiceImpl.class);
    
    private final UserActivityLogRepository activityLogRepository;

    private final UserRepository userRepository;
    
    public UserActivityServiceImpl(UserActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public void logActivity(Long userId, String userEmail, String userRole,
                           ActivityType activityType, String description,
                           HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            UserActivityLog activityLog = UserActivityLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .activityType(activityType)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            
            activityLogRepository.save(activityLog);
            log.info("Activity logged: {} for user {}", activityType, userEmail);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }
    
    @Override
    public void logActivity(Long userId, String userEmail, String userRole,
                           ActivityType activityType, String description) {
        try {
            UserActivityLog activityLog = UserActivityLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .activityType(activityType)
                    .description(description)
                    .build();
            
            activityLogRepository.save(activityLog);
            log.info("Activity logged: {} for user {}", activityType, userEmail);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }
    
    @Override
    public Page<UserActivityLog> getUserActivityHistory(Long userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
    
    @Override
    public Instant findLastLoginTime(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return activityLogRepository.findLastLoginTime(userId);
    }

    @Override
    public long countRecentLogins(Long userId, int hours) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return activityLogRepository.countLoginsSince(userId, since);
    }
    
    @Override
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Instant now = Instant.now();

        stats.put("totalLogins", activityLogRepository.countTotalLogins());
        stats.put("totalRegistrations", activityLogRepository.countTotalRegistrations());

        // Today's activity (UTC Day)
        Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);

        stats.put("loginsToday", activityLogRepository.countByActivityTypeAndDateRange(
                ActivityType.LOGIN, startOfDay, now));
        stats.put("registrationsToday", activityLogRepository.countByActivityTypeAndDateRange(
                ActivityType.REGISTRATION, startOfDay, now));

        // This week's activity
        Instant startOfWeek = now.minus(7, ChronoUnit.DAYS);
        stats.put("loginsThisWeek", activityLogRepository.countByActivityTypeAndDateRange(
                ActivityType.LOGIN, startOfWeek, now));
        stats.put("registrationsThisWeek", activityLogRepository.countByActivityTypeAndDateRange(
                ActivityType.REGISTRATION, startOfWeek, now));

        return stats;
//        Map<String, Object> stats = new HashMap<>();
//
//        // Total counts
//        stats.put("totalLogins", activityLogRepository.countTotalLogins());
//        stats.put("totalRegistrations", activityLogRepository.countTotalRegistrations());
//
//        // Today's activity
//        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
//        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
//
//        stats.put("loginsToday", activityLogRepository.countByActivityTypeAndDateRange(
//                ActivityType.LOGIN, startOfDay, endOfDay));
//        stats.put("registrationsToday", activityLogRepository.countByActivityTypeAndDateRange(
//                ActivityType.REGISTRATION, startOfDay, endOfDay));
//
//        // This week's activity
//        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
//        stats.put("loginsThisWeek", activityLogRepository.countByActivityTypeAndDateRange(
//                ActivityType.LOGIN, startOfWeek, LocalDateTime.now()));
//        stats.put("registrationsThisWeek", activityLogRepository.countByActivityTypeAndDateRange(
//                ActivityType.REGISTRATION, startOfWeek, LocalDateTime.now()));
//
//        return stats;
    }
    
    @Override
    public Page<UserActivityLog> getRecentActivities(Pageable pageable) {
        return activityLogRepository.findRecentActivities(pageable);
    }
    
    @Override
    public Page<UserActivityLog> getAdminActivities(Pageable pageable) {
        return activityLogRepository.findByUserRoleOrderByTimestampDesc("ADMIN",pageable);
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
