package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.ProductActivityDTOs.ProductActivityResponseDTO;
import com.shop.productservice.entity.ProductActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductActivityMapper {

    /**
     * Convert Entity -> Response DTO
     */
    public ProductActivityResponseDTO toResponseDTO(ProductActivityLog activity) {

        if (activity == null) {
            return null;
        }

        return ProductActivityResponseDTO.builder()
                .id(activity.getId())

                // Admin Information
                .userEmail(activity.getUserEmail())
                .userRole(activity.getUserRole())

                // Target Information
                .targetId(activity.getTargetId())
                .activityType(activity.getActivityType())

                // Activity Details
                .description(activity.getDescription())

                // Request Information
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())

                // Audit Timestamp
                .timestamp(activity.getTimestamp())

                .build();
    }

    /**
     * Convert List<Entity> -> List<ResponseDTO>
     */
    public List<ProductActivityResponseDTO> toResponseDTOs(
            List<ProductActivityLog> activities) {

        if (activities == null || activities.isEmpty()) {
            return Collections.emptyList();
        }

        return activities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Page<Entity> -> Page<ResponseDTO>
     */
    public Page<ProductActivityResponseDTO> toResponseDTOPage(
            Page<ProductActivityLog> activities) {

        if (activities == null) {
            return Page.empty();
        }

        return activities.map(this::toResponseDTO);
    }

}
