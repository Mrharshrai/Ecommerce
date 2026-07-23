package com.shop.productservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE,
    FEMALE,
    UNISEX,
    BOYS,
    GIRLS,
    KIDS,
    BABY;

    @JsonCreator
    public static Gender fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }
        for (Gender g : Gender.values()) {
            if (g.name().equalsIgnoreCase(value.trim())) {
                return g;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + value);
    }
}

