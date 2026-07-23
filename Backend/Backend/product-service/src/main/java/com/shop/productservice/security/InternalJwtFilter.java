package com.shop.productservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Security filter that validates internal JWT tokens on /internal/** routes.
 * Only runs for /internal/** paths — all other paths are skipped.
 * The regular JwtAuthFilter already skips /internal/**, so these two filters
 * handle different path spaces with no overlap.
 */
@Component
public class InternalJwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalJwtFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final InternalJwtUtil internalJwtUtil;

    public InternalJwtFilter(InternalJwtUtil internalJwtUtil) {
        this.internalJwtUtil = internalJwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // This filter ONLY runs for /internal/** paths
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Require Authorization header
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            logAndRespondError(response, "Internal token required", request.getRequestURI());
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 2. Validate with the internal secret (not the customer JWT secret)
            if (!internalJwtUtil.validateToken(token)) {
                logAndRespondError(response, "Invalid or expired internal token", request.getRequestURI());
                return;
            }

            // 3. Extract calling service name (e.g. "cart-service")
            String serviceName = internalJwtUtil.extractServiceName(token);

            // 4. Grant INTERNAL authority
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("INTERNAL")
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    serviceName != null ? serviceName : "internal-service",
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Internal auth granted for service: {} on path: {}", serviceName, request.getRequestURI());

            // 5. Continue the chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Internal authentication failed for {}: {}", request.getRequestURI(), e.getMessage());
            logAndRespondError(response, "Internal security verification failed", request.getRequestURI());
        }
    }

    private void logAndRespondError(HttpServletResponse response, String message, String uri) throws IOException {
        logger.warn("Security Alert: {} for URI: {}", message, uri);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
    }
}
