package com.shop.userservice.user_service.repository;

import com.shop.userservice.user_service.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    
    Optional<TokenBlacklist> findByToken(String token);
    
    boolean existsByToken(String token);
    
    void deleteByExpiryTimeBefore(Instant now);
}
