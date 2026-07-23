package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.TokenBlacklist;
import com.shop.userservice.user_service.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);
    
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistServiceImpl(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    @Transactional
    public void blacklistToken(String token, long expiryTimeMillis) {
        // Using Instant directly.
        // We assume expiryTimeMillis is the calculated expiration timestamp from the JWT.
        Instant expiryInstant = Instant.now().plusMillis(expiryTimeMillis);

        TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                .token(token)
                .expiryTime(expiryInstant) // Saved as Instant
                .build();

        tokenBlacklistRepository.save(blacklistedToken);
        log.info("Token blacklisted. Database will hold it until UTC: {}", expiryInstant);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 4 * * *", zone = "UTC") // Run daily at 4 AM
    // sec - min - hour - Every day of month- Every month - Every day of the week.
    public void cleanupExpiredTokens() {
        // Ensure the cleanup 'now' is also UTC-aligned
        Instant now = Instant.now();
        tokenBlacklistRepository.deleteByExpiryTimeBefore(now);
        log.info("Cleaned up expired blacklisted tokens at UTC: {}", now);
    }
}
