package com.shop.productservice.repository;

import com.shop.productservice.entity.ProductActivityLog;
import com.shop.productservice.enums.ProductActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProductActivityLogRepository extends JpaRepository<ProductActivityLog, Long> {

    /**
     * Product / Discount / Related Product history
     */
    Page<ProductActivityLog> findByTargetIdAndActivityTypeInOrderByTimestampDesc(
            Long targetId,
            List<ProductActivityType> activityTypes,
            Pageable pageable
    );

    /**
     * Activities performed by a specific admin
     */
    Page<ProductActivityLog> findByUserEmailOrderByTimestampDesc(
            String userEmail,
            Pageable pageable
    );

    /**
     * Latest activities
     */
    @Query("""
            SELECT a
            FROM ProductActivityLog a
            ORDER BY a.timestamp DESC
            """)
    Page<ProductActivityLog> findRecentActivities(Pageable pageable);

    /**
     * Filter by activity type
     */
    Page<ProductActivityLog> findByActivityTypeOrderByTimestampDesc(
            ProductActivityType activityType,
            Pageable pageable
    );

    /**
     * Filter by date range
     */
    Page<ProductActivityLog> findByTimestampBetweenOrderByTimestampDesc(
            Instant start,
            Instant end,
            Pageable pageable
    );

    /**
     * Search by description
     */
    Page<ProductActivityLog> findByDescriptionContainingIgnoreCaseOrderByTimestampDesc(
            String keyword,
            Pageable pageable
    );

    /**
     * =============================================================
     * Total number of activity logs.
     * =============================================================
     */
    long count();

    /**
     * =============================================================
     * Count activities by a list of activity types.
     *
     * Used for:
     * • Product Activity Count
     * • Variant Activity Count
     * • Size Activity Count
     * • Image Activity Count
     * • Related Product Activity Count
     * • Discount Activity Count
     * =============================================================
     */
    long countByActivityTypeIn(List<ProductActivityType> activityTypes);

    /**
     * =============================================================
     * Count today's activities.
     * =============================================================
     */
    long countByTimestampAfter(Instant startOfDay);

    /**
     * =============================================================
     * Count activities after a specific timestamp.
     *
     * Used for:
     * • This Week
     * • This Month
     * =============================================================
     */
    long countByTimestampGreaterThanEqual(Instant start);

    /**
     * =============================================================
     * Returns activity count grouped by Activity Type.
     *
     * Used for:
     * Activity Statistics
     * =============================================================
     */
    @Query("""
       SELECT a.activityType,
              COUNT(a)
       FROM ProductActivityLog a
       GROUP BY a.activityType
       """)
    List<Object[]> countActivitiesByType();

    /**
     * =============================================================
     * Returns activity count grouped by Admin Email.
     *
     * Used for:
     * Activity Statistics
     * =============================================================
     */
    @Query("""
       SELECT a.userEmail,
              COUNT(a)
       FROM ProductActivityLog a
       GROUP BY a.userEmail
       """)
    List<Object[]> countActivitiesByAdmin();

    /**
     * =============================================================
     * Returns activity count grouped by Date.
     *
     * Used for dashboard graphs.
     * =============================================================
     */
    @Query("""
       SELECT FUNCTION('DATE', a.timestamp),
              COUNT(a)
       FROM ProductActivityLog a
       GROUP BY FUNCTION('DATE', a.timestamp)
       ORDER BY FUNCTION('DATE', a.timestamp)
       """)
    List<Object[]> countDailyActivities();

    /**
     * =============================================================
     * Returns all distinct admin emails.
     *
     * Used for filter dropdown.
     * =============================================================
     */
    @Query("""
       SELECT DISTINCT a.userEmail
       FROM ProductActivityLog a
       ORDER BY a.userEmail
       """)
    List<String> findDistinctUserEmails();

    /**
     * =============================================================
     * Returns all distinct user roles.
     *
     * Used for filter dropdown.
     * =============================================================
     */
    @Query("""
       SELECT DISTINCT a.userRole
       FROM ProductActivityLog a
       ORDER BY a.userRole
       """)
    List<String> findDistinctUserRoles();

    /**
     * =============================================================
     * Returns earliest activity timestamp.
     *
     * Used for filter date picker.
     * =============================================================
     */
    @Query("""
       SELECT MIN(a.timestamp)
       FROM ProductActivityLog a
       """)
    Instant findOldestActivityTimestamp();

    /**
     * =============================================================
     * Returns latest activity timestamp.
     *
     * Used for filter date picker.
     * =============================================================
     */
    @Query("""
       SELECT MAX(a.timestamp)
       FROM ProductActivityLog a
       """)
    Instant findLatestActivityTimestamp();

    /**
     * =============================================================
     * Returns total distinct admins who have performed activities.
     * Used for:
     * Dashboard Summary
     * =============================================================
     */
    @Query("""
       SELECT COUNT(DISTINCT a.userEmail)
       FROM ProductActivityLog a
       """)
    Long countDistinctAdmins();



}
