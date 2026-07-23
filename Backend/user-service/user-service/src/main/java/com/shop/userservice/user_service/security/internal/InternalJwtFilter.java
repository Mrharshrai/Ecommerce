package com.shop.userservice.user_service.security.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class InternalJwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalJwtFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final InternalJwtUtil internalJwtUtil;

    // Use constructor injection
    public InternalJwtFilter(InternalJwtUtil internalJwtUtil) {
        this.internalJwtUtil = internalJwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // This filter ONLY runs for paths starting with /internal/
        return !path.startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Header Presence Check
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            logAndRespondError(response, "Internal token required", request.getRequestURI());
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 2. Token Validation (Uses a separate Internal Secret)
            if (!internalJwtUtil.validateToken(token)) {
                logAndRespondError(response, "Invalid or expired internal token", request.getRequestURI());
                return;
            }

            // 3. Extract service name from token if available, otherwise use a generic constant
            String serviceName = internalJwtUtil.extractServiceName(token); // e.g., "inventory-service"

            // Assign specialized internal role
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("INTERNAL")
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    serviceName != null ? serviceName : "internal-service",
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. Continue the chain
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
