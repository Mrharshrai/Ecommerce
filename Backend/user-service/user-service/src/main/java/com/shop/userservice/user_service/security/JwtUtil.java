package com.shop.userservice.user_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (this.secret == null || this.secret.isBlank()) {
            throw new IllegalStateException("JWT Secret is null! Check configuration.");
        }

        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= TOKEN GENERATION =================

    public String generateToken(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // keep same key (IMPORTANT)

        return buildToken(claims, authentication.getName(), jwtExpiration);
    }

    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationTime) {
        // Use Instant for UTC timeline consistency
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationTime);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))       // Convert Instant to Date
                .setExpiration(Date.from(expiry)) // Convert Instant to Date
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // ================= EXTRACTION =================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the username (subject) from a token even if it is expired.
     * Used during logout so we can still clear the DB refresh token when
     * the cookie has expired. Returns null only if the token is truly
     * malformed or has an invalid signature (i.e. tampered).
     */
    public String extractUsernameIgnoreExpiry(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            // Token is expired but structurally valid — claims are still accessible
            return e.getClaims().getSubject();
        } catch (Exception e) {
            logger.warn("Could not extract username (ignoring expiry): {}", e.getMessage());
            return null;
        }
    }

    /**
     * 🔥 FIXED METHOD (CRITICAL)
     * - Never returns null
     * - Handles type safely
     * - Works for both access + refresh tokens
     */
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);

            Object rolesObj = claims.get("roles");

            if (rolesObj == null) {
                return Collections.emptyList(); // ✅ NEVER null
            }

            return ((List<?>) rolesObj)
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.warn("Failed to extract roles: {}", e.getMessage());
            return Collections.emptyList(); // ✅ fail-safe
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.warn("Failed to extract claim: {}", e.getMessage());
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ================= VALIDATION =================

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Invalid signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Empty claims: {}", e.getMessage());
        }
        return false;
    }
}

