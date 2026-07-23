package com.shop.userservice.user_service.exception;

public class RateLimitException  extends RuntimeException{
    public RateLimitException(String message) {
        super(message);
    }
}
