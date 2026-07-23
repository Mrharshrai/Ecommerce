package com.shop.productservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AgeGroup {

    Infant_0_12M,
    Toddler_1_3Y,
    Kids_4_6Y,
    Kids_7_12Y,
    Teen_13_19Y,
    Adult,
    Senior;

    @JsonCreator
    public static AgeGroup fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("AgeGroup cannot be null or empty");
        }
        for (AgeGroup a : AgeGroup.values()) {
            if (a.name().equalsIgnoreCase(value.trim())) {
                return a;
            }
        }
        throw new IllegalArgumentException("Invalid ageGroup: " + value);
    }
}

