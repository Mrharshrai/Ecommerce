package com.shop.productservice.exception;

public class VariantNotFoundException extends RuntimeException {
    public VariantNotFoundException(String message) {
        super(message);
    }
}
