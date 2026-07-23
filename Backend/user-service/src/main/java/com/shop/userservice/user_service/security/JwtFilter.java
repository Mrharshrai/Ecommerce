package com.shop.userservice.user_service.security;

import com.shop.userservice.user_service.service.TokenBlacklistService;
import com.shop.userservice.user_service.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    // Constructor Injection: Preferred over @Autowired on fields for testability and immutability
    public JwtFilter(JwtUtil jwtUtil,
                     UserDetailsServiceImpl userDetailsService,
                     TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Bypass internal calls or public auth endpoints if necessary
        return path.startsWith("/internal/") || path.startsWith("/api/v1/auth/public");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Validation of Header Structure
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 2. Check Blacklist (Database or Redis hit)
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.warn("Attempted access with blacklisted token");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
                return;
            }

            final String username = jwtUtil.extractUsername(token);

            // 3. Authenticate if username is present and SecurityContext is empty
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token signature and expiration
                if (jwtUtil.validateToken(token)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Extract authorities directly from JWT to avoid extra DB joins if possible
                    List<GrantedAuthority> authorities = jwtUtil.extractRoles(token).stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);

                    // Add request-specific details (IP, Session ID) to the auth object
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (UsernameNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User record not found");
            return;
        } catch (Exception e) {
            logger.error("JWT Authentication failed: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired session");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to send consistent JSON error responses
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String jsonPayload = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        response.getWriter().write(jsonPayload);
    }
}