package com.shop.productservice.service.activityService;

import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityFilterResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityStatsResponseDTO;
import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivitySummaryResponseDTO;
import com.shop.productservice.entity.ProductActivityLog;
import com.shop.productservice.enums.ProductActivityType;
import com.shop.productservice.mapper.ProductActivityMapper;
import com.shop.productservice.repository.ProductActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ProductActivityServiceImpl implements ProductActivityService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductActivityServiceImpl.class);
    
    private final ProductActivityLogRepository activityLogRepository;
    private final ProductActivityMapper productActivityMapper;

    public ProductActivityServiceImpl(
            ProductActivityLogRepository activityLogRepository,
            ProductActivityMapper productActivityMapper) {

        this.activityLogRepository = activityLogRepository;
        this.productActivityMapper = productActivityMapper;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(String userEmail, String userRole,
                           Long targetId, ProductActivityType activityType, String description,
                           HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request != null ? request.getHeader("User-Agent") : null;
            
            ProductActivityLog activityLog = ProductActivityLog.builder()
                    .userId(0L)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .targetId(targetId)
                    .activityType(activityType)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            
            activityLogRepository.save(activityLog);
            log.info("Activity logged: {} for product {} by user {}", activityType, targetId, userEmail);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(String userEmail, String userRole,
                           Long targetId, ProductActivityType activityType, String description) {
        try {
            ProductActivityLog activityLog = ProductActivityLog.builder()
                    .userId(0L)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .targetId(targetId)
                    .activityType(activityType)
                    .description(description)
                    .build();
            
            activityLogRepository.save(activityLog);
            log.info("Activity logged: {} for product {} by user {}", activityType, targetId, userEmail);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductActivityResponseDTO> getProductActivityHistory(
            Long productId,
            Pageable pageable) {

        Page<ProductActivityLog> activities =
                activityLogRepository.findByTargetIdAndActivityTypeInOrderByTimestampDesc(
                        productId,
                        getProductActivityTypes(),
                        pageable
                );

        return productActivityMapper.toResponseDTOPage(activities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductActivityResponseDTO> getDiscountActivityHistory(
            Long discountId,
            Pageable pageable) {

        Page<ProductActivityLog> activities =
                activityLogRepository.findByTargetIdAndActivityTypeInOrderByTimestampDesc(
                        discountId,
                        getDiscountActivityTypes(),
                        pageable
                );

        return productActivityMapper.toResponseDTOPage(activities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductActivityResponseDTO> getRelatedProductActivityHistory(
            Long productId,
            Pageable pageable) {

        Page<ProductActivityLog> activities =
                activityLogRepository.findByTargetIdAndActivityTypeInOrderByTimestampDesc(
                        productId,
                        getRelatedProductActivityTypes(),
                        pageable
                );

        return productActivityMapper.toResponseDTOPage(activities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductActivityResponseDTO> getRecentActivities(
            Pageable pageable) {

        Page<ProductActivityLog> activities =
                activityLogRepository.findRecentActivities(pageable);

        return productActivityMapper.toResponseDTOPage(activities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductActivityResponseDTO> getAdminActivityHistory(
            String adminEmail,
            Pageable pageable) {

        Page<ProductActivityLog> activities =
                activityLogRepository.findByUserEmailOrderByTimestampDesc(
                        adminEmail,
                        pageable
                );

        return productActivityMapper.toResponseDTOPage(activities);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductActivitySummaryResponseDTO getActivitySummary() {

        ZoneId utc = ZoneOffset.UTC;

        Instant startOfToday = LocalDate.now(utc)
                .atStartOfDay(utc)
                .toInstant();

        Instant startOfWeek = LocalDate.now(utc)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(utc)
                .toInstant();

        Instant startOfMonth = LocalDate.now(utc)
                .withDayOfMonth(1)
                .atStartOfDay(utc)
                .toInstant();

        List<ProductActivityType> productTypes = List.of(
                ProductActivityType.PRODUCT_CREATED,
                ProductActivityType.PRODUCT_UPDATED,
                ProductActivityType.PRODUCT_PUBLISHED,
                ProductActivityType.PRODUCT_UNPUBLISHED,
                ProductActivityType.PRODUCT_ACTIVATED,
                ProductActivityType.PRODUCT_DEACTIVATED,
                ProductActivityType.PRODUCT_DELETED,
                ProductActivityType.PRODUCT_RESTORED
        );

        List<ProductActivityType> variantTypes = List.of(
                ProductActivityType.VARIANT_CREATED,
                ProductActivityType.VARIANT_UPDATED,
                ProductActivityType.VARIANT_ACTIVATED,
                ProductActivityType.VARIANT_DEACTIVATED,
                ProductActivityType.VARIANT_DELETED,
                ProductActivityType.VARIANT_RESTORED
        );

        List<ProductActivityType> sizeTypes = List.of(
                ProductActivityType.SIZE_CREATED,
                ProductActivityType.SIZE_UPDATED,
                ProductActivityType.SIZE_ACTIVATED,
                ProductActivityType.SIZE_DEACTIVATED,
                ProductActivityType.SIZE_DELETED,
                ProductActivityType.SIZE_RESTORED
        );

        List<ProductActivityType> imageTypes = List.of(
                ProductActivityType.IMAGE_CREATED,
                ProductActivityType.IMAGE_UPDATED,
                ProductActivityType.IMAGE_ACTIVATED,
                ProductActivityType.IMAGE_DEACTIVATED,
                ProductActivityType.IMAGE_DELETED,
                ProductActivityType.IMAGE_RESTORED
        );

        return ProductActivitySummaryResponseDTO.builder()
                .totalActivities(activityLogRepository.count())
                .totalProductActivities(activityLogRepository.countByActivityTypeIn(productTypes))
                .totalVariantActivities(activityLogRepository.countByActivityTypeIn(variantTypes))
                .totalSizeActivities(activityLogRepository.countByActivityTypeIn(sizeTypes))
                .totalImageActivities(activityLogRepository.countByActivityTypeIn(imageTypes))
                .totalRelatedProductActivities(activityLogRepository.countByActivityTypeIn(getRelatedProductActivityTypes()))
                .totalDiscountActivities(activityLogRepository.countByActivityTypeIn(getDiscountActivityTypes()))
                .totalActiveAdmins(activityLogRepository.countDistinctAdmins())
                .todayActivities(activityLogRepository.countByTimestampAfter(startOfToday))
                .thisWeekActivities(activityLogRepository.countByTimestampGreaterThanEqual(startOfWeek))
                .thisMonthActivities(activityLogRepository.countByTimestampGreaterThanEqual(startOfMonth))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductActivityStatsResponseDTO getActivityStatistics() {

        Map<ProductActivityType, Long> activityTypeStats = new EnumMap<>(ProductActivityType.class);

        for (Object[] row : activityLogRepository.countActivitiesByType()) {
            activityTypeStats.put(
                    (ProductActivityType) row[0],
                    ((Number) row[1]).longValue()
            );
        }

        Map<String, Long> adminStats = new LinkedHashMap<>();

        for (Object[] row : activityLogRepository.countActivitiesByAdmin()) {
            adminStats.put(
                    (String) row[0],
                    ((Number) row[1]).longValue()
            );
        }

        Map<LocalDate, Long> dailyStats = new TreeMap<>();

        for (Object[] row : activityLogRepository.countDailyActivities()) {

            LocalDate date;

            if (row[0] instanceof LocalDate localDate) {
                date = localDate;
            } else if (row[0] instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else {
                date = LocalDate.parse(row[0].toString());
            }

            dailyStats.put(
                    date,
                    ((Number) row[1]).longValue()
            );
        }

        return ProductActivityStatsResponseDTO.builder()
                .activityCountByType(activityTypeStats)
                .activityCountByAdmin(adminStats)
                .dailyActivityCount(dailyStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductActivityFilterResponseDTO getActivityFilters() {

        return ProductActivityFilterResponseDTO.builder()
                .adminEmails(activityLogRepository.findDistinctUserEmails())
                .userRoles(activityLogRepository.findDistinctUserRoles())
                .activityTypes(Arrays.asList(ProductActivityType.values()))
                .oldestActivity(activityLogRepository.findOldestActivityTimestamp())
                .latestActivity(activityLogRepository.findLatestActivityTimestamp())
                .totalActivities(activityLogRepository.count())
                .build();
    }


    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        
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

    /**
     * =============================================================
     * Returns all activity types that belong to a Product.
     *
     * This includes:
     * • Product
     * • Variant
     * • Size
     * • Image
     * • Related Product
     *
     * Used for:
     *      Product History API
     * =============================================================
     */
    private List<ProductActivityType> getProductActivityTypes() {

        return List.of(

                // ---------------- Product ----------------
                ProductActivityType.PRODUCT_CREATED,
                ProductActivityType.PRODUCT_UPDATED,
                ProductActivityType.PRODUCT_PUBLISHED,
                ProductActivityType.PRODUCT_UNPUBLISHED,
                ProductActivityType.PRODUCT_ACTIVATED,
                ProductActivityType.PRODUCT_DEACTIVATED,
                ProductActivityType.PRODUCT_DELETED,
                ProductActivityType.PRODUCT_RESTORED,

                // ---------------- Variant ----------------
                ProductActivityType.VARIANT_CREATED,
                ProductActivityType.VARIANT_UPDATED,
                ProductActivityType.VARIANT_ACTIVATED,
                ProductActivityType.VARIANT_DEACTIVATED,
                ProductActivityType.VARIANT_DELETED,
                ProductActivityType.VARIANT_RESTORED,

                // ---------------- Size ----------------
                ProductActivityType.SIZE_CREATED,
                ProductActivityType.SIZE_UPDATED,
                ProductActivityType.SIZE_ACTIVATED,
                ProductActivityType.SIZE_DEACTIVATED,
                ProductActivityType.SIZE_DELETED,
                ProductActivityType.SIZE_RESTORED,

                // ---------------- Image ----------------
                ProductActivityType.IMAGE_CREATED,
                ProductActivityType.IMAGE_UPDATED,
                ProductActivityType.IMAGE_ACTIVATED,
                ProductActivityType.IMAGE_DEACTIVATED,
                ProductActivityType.IMAGE_DELETED,
                ProductActivityType.IMAGE_RESTORED,

                // ---------------- Related Product ----------------
                ProductActivityType.RELATED_PRODUCT_CREATED,
                ProductActivityType.RELATED_PRODUCT_ACTIVATED,
                ProductActivityType.RELATED_PRODUCT_DEACTIVATED,
                ProductActivityType.RELATED_PRODUCT_DELETED,
                ProductActivityType.RELATED_PRODUCT_RESTORED
        );
    }

    /**
     * =============================================================
     * Returns all activity types related to Discounts.
     *
     * Used for:
     *      Discount History API
     * =============================================================
     */
    private List<ProductActivityType> getDiscountActivityTypes() {

        return List.of(

                ProductActivityType.DISCOUNT_CREATED,
                ProductActivityType.DISCOUNT_UPDATED,
                ProductActivityType.DISCOUNT_ACTIVATED,
                ProductActivityType.DISCOUNT_DEACTIVATED,
                ProductActivityType.DISCOUNT_DELETED
        );
    }

    /**
     * =============================================================
     * Returns only Related Product activity types.
     *
     * Used for:
     *      Related Product History API
     * =============================================================
     */
    private List<ProductActivityType> getRelatedProductActivityTypes() {

        return List.of(

                ProductActivityType.RELATED_PRODUCT_CREATED,
                ProductActivityType.RELATED_PRODUCT_ACTIVATED,
                ProductActivityType.RELATED_PRODUCT_DEACTIVATED,
                ProductActivityType.RELATED_PRODUCT_DELETED,
                ProductActivityType.RELATED_PRODUCT_RESTORED
        );
    }
}
