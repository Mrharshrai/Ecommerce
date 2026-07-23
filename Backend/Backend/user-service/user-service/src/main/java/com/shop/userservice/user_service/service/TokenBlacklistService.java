package com.shop.userservice.user_service.service;

public interface TokenBlacklistService {
    
    void blacklistToken(String token, long expiryTimeMillis);
    
    boolean isTokenBlacklisted(String token);
    
    void cleanupExpiredTokens();
}
