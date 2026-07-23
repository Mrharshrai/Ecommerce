package com.shop.productservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Size {
    XS,
    S,
    M,
    L,
    XL,
    XXL,
    XXXL,
    FS;

    @JsonCreator
    public static Size fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Size cannot be null or empty");
        }

        for (Size s : Size.values()) {
            if (s.name().equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid size: " + value);
    }
}

