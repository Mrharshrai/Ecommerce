package com.shop.productservice.DTOs.ReviewDTOs.ResponseDTOs;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingInfo {

    private Double averageRating;
    private Long reviewCount;
    private Map<Integer, Long> ratingDistribution;
}
