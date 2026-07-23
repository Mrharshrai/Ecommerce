package com.shop.productservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        Jws<Claims> parsed = Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
        return parsed.getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("roles", List.class); // assuming your token has a "roles" claim
    }

}

