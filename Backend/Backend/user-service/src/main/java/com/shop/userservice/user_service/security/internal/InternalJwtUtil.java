package com.shop.userservice.user_service.security.internal;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.time.Instant; // Added
import java.time.temporal.ChronoUnit;
@Component
public class InternalJwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(InternalJwtUtil.class);

    @Value("${internal.jwt.secret}")
    private String secret;

    @Value("${internal.jwt.expiration}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Ensure this secret is different and longer (at least 64 chars for HS512)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a token specifically for service-to-service auth.
     * Tip: Pass a 'clientName' parameter if you want to track which service is calling.
     */
    public String generateInternalToken(String serviceName) {
        // Use Instant for precise UTC timeline
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expiration);

        return Jwts.builder()
                .setSubject(serviceName)
                .claim("authorities", List.of("INTERNAL"))
                .setIssuedAt(Date.from(now))       // Convert Instant to Date
                .setExpiration(Date.from(expiry)) // Convert Instant to Date
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Internal token expired: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Internal token signature mismatch! Possible intrusion attempt.");
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.warn("Invalid internal token format: {}", e.getMessage());
        }
        return false;
    }

    public String extractServiceName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

