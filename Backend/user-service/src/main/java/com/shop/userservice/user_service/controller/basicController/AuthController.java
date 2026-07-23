package com.shop.userservice.user_service.controller.basicController;

import com.shop.userservice.user_service.dto.requestDTO.LoginRequest;
import com.shop.userservice.user_service.dto.requestDTO.ResetPasswordDTO;
import com.shop.userservice.user_service.dto.responseDTO.LoginResponse;
import com.shop.userservice.user_service.dto.responseDTO.UserRegistrationResponseDTO;
import com.shop.userservice.user_service.entity.ActivityType;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.exception.InvalidCredentialsException;
import com.shop.userservice.user_service.mapper.UserMapper;
import com.shop.userservice.user_service.security.JwtUtil;
import com.shop.userservice.user_service.service.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private final VerificationService verificationService;
    private final UserActivityService activityService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpiryMs;

    @Value("${jwt.cookie-secure}")
    private boolean isCookieSecure;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService,
                          UserMapper userMapper,
                          TokenBlacklistService tokenBlacklistService,
                          VerificationService verificationService,
                          UserActivityService activityService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userMapper = userMapper;
        this.tokenBlacklistService = tokenBlacklistService;
        this.verificationService = verificationService;
        this.activityService = activityService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                               jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            // 1. Check if user exists and is NOT deleted
            // (Handles: "only non deleted emails can login, else > register first")
            User user = userService.findNonDeletedUserByEmail(request.getEmail());

            // 2. Manual Status Check BEFORE Authentication (Fixes your "Locked" issue)
            // If they are inactive customer, we handle it here.
            // If they are Admin/Driver, this throws the "Contact Support" exception immediately.
            if (!user.isActive()) {
                userService.handleInactiveUser(user);
            }

            // 3. Authenticate Password
            // (Handles: "if password wrong > throw excp")
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );
            } catch (BadCredentialsException e) {
                throw new InvalidCredentialsException("Password is incorrect");
            } catch (LockedException | DisabledException e) {
                // If Spring Security still throws this, we've already handled
                // the customer activation above, so this would only hit for
                // actual security-level locks.
                throw new InvalidCredentialsException("Account access restricted. Contact support.");
            }

            // 4. Set Context & Generate Tokens
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(request.getEmail());

//            userService.saveRefreshToken(user, refreshToken,
//                    java.time.LocalDateTime.now().plusDays(refreshTokenExpiryDays));

            // Use plus(ms, ChronoUnit.MILLIS) or convert ms to days if you want to stick with plusDays
            userService.saveRefreshToken(user, refreshToken,
                    java.time.Instant.now().plus(refreshTokenExpiryMs, java.time.temporal.ChronoUnit.MILLIS));

            // 5. Activity Logging
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    user.getRoles().iterator().next().name(),
                    ActivityType.LOGIN,
                    "User logged in successfully",
                    httpRequest
            );

            // 6. Create the HttpOnly Cookie
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(isCookieSecure)
//                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiryMs / 1000) // Convert ms to seconds
                    .sameSite("Strict")
                    .build();

            UserRegistrationResponseDTO responseDTO = userMapper.toDTO(user);

            // 7. Add the refreshToken in header and return refreshToken as null in the body
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new LoginResponse(token, null, responseDTO)); // null here forces the frontend to use the cookie

        } catch (InvalidCredentialsException e) {
            // Catch our custom messages (User not found, Password wrong)
            throw e;
        } catch (Exception e) {
            logger.error("Login failed for email: {}", request.getEmail(), e);
            throw new InvalidCredentialsException(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. Extract from HttpOnly Cookie instead of Body
        String refreshToken = null;
        try {
        if (request.getCookies() != null) {
            refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token missing");
        }


            // 2. Validate signature and database state
            String username = jwtUtil.extractUsername(refreshToken);
            
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token. Please log in again.");
            }

            User user = userService.validateAndRotateRefreshToken(username, refreshToken);

            // 3. Generate New Access Token
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());

            Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
            String newAccessToken = jwtUtil.generateToken(auth);

            // 4. Set NEW Refresh Token in HttpOnly Cookie (Rotation)
            ResponseCookie cookie = ResponseCookie.from("refreshToken", user.getRefreshToken())
                    .httpOnly(true)
                    .secure(isCookieSecure)
//                    .secure(true) // Set to true in production (HTTPS)
                    .path("/")
                    .maxAge(refreshTokenExpiryMs / 1000)
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of("token", newAccessToken));

        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            // Must return 401 so the frontend knows to redirect to login, NOT 500.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token. Please log in again.");
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (LockedException | DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            // This will show you the real error in the Postman response body
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Accepts an email address or a mobile number and dispatches a
     * FORGOT_PASSWORD OTP via the appropriate channel.
     * All business logic (user lookup, OTP generation, dispatch) is
     * handled by {@link VerificationService}.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String identifier = request.get("key");

        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body("Request key must not be empty.");
        }
        final String emailPattern  = "^[A-Za-z0-9][A-Za-z0-9._%+\\-]*@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";
        final String mobilePattern = "^\\+[1-9]\\d{0,2}\\d{10}$";

        if (identifier.matches(emailPattern)) {
            return verificationService.sendForgotPasswordOtpByEmail(identifier);
        } else if (identifier.matches(mobilePattern)) {
            return verificationService.sendForgotPasswordOtpByMobile(identifier);
        } else {
            if (identifier.matches("^[+0-9].*")) {
                return ResponseEntity.badRequest().body("Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)");
            }
            return ResponseEntity.badRequest().body("Invalid input. Please enter a valid email or mobile number.");
        }
    }

    /**
     * Accepts an email address or a mobile number together with the
     * FORGOT_PASSWORD OTP and the desired new password.
     * Delegates all OTP verification and password-update logic to
     * {@link VerificationService}.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordDTO request) {
        String identifier  = request.getKey();
        String otp         = request.getOtp();
        String newPassword = request.getNewPassword();

        final String emailPattern  = "^[A-Za-z0-9][A-Za-z0-9._%+\\-]*@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";
        final String mobilePattern = "^\\+[1-9]\\d{0,2}\\d{10}$";

        if (identifier.matches(emailPattern)) {
            return verificationService.resetPasswordByEmail(identifier, otp, newPassword);
        } else if (identifier.matches(mobilePattern)) {
            return verificationService.resetPasswordByMobile(identifier, otp, newPassword);
        } else {
            if (identifier.matches("^[+0-9].*")) {
                return ResponseEntity.badRequest().body("Mobile number must start with '+' followed by country code and 10 digits (e.g. +919876543210)");
            }
            return ResponseEntity.badRequest().body("Invalid input. Please enter a valid email or mobile number.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        try {
            // 1. Blacklist Access Token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    tokenBlacklistService.blacklistToken(token, jwtExpiration);
                } catch (Exception e) {
                    // Log and continue—we still want to try clearing the cookie
                    logger.error("Failed to blacklist access token during logout: {}", e.getMessage());
                }
            }

            // 2. Clear DB Refresh Token
            // Try to get username from the Refresh Cookie instead of the Bearer token
            if (request.getCookies() != null) {
                Arrays.stream(request.getCookies())
                        .filter(c -> "refreshToken".equals(c.getName()))
                        .findFirst()
                        .ifPresent(c -> {
                            try {
                                // Logout Bug Fix: use extractUsernameIgnoreExpiry so we can
                                // still clear the DB token even when the cookie has expired.
                                // Regular extractUsername() silently returns null for expired
                                // tokens, leaving an orphaned record in the database.
                                String username = jwtUtil.extractUsernameIgnoreExpiry(c.getValue());
                                if (username != null) {
                                    User user = userService.findNonDeletedUserByEmail(username);

                                    // Activity Logging (Logged right before tracking session record drops)
                                    activityService.logActivity(
                                            user.getId(),
                                            user.getEmail(),
                                            user.getRoles().iterator().next().name(),
                                            ActivityType.LOGOUT, // Replace with your exact enum variant
                                            "User logged out successfully",
                                            request
                                    );
                                    userService.clearRefreshToken(username);
                                } else {
                                    // Token is malformed/tampered — nothing valid to clear
                                    logger.warn("Refresh cookie was malformed or tampered; skipping DB clear.");
                                }
                            } catch (Exception e) {
                                logger.warn("Could not clear refresh token during logout: {}", e.getMessage());
                            }
                        });
            }
        } catch (Exception e) {
            // Ultimate fallback to ensure a crash doesn't stop the cookie clearing
            logger.error("Unexpected error during logout processing: {}", e.getMessage());
        } finally {

            // 3. ALWAYS Clear the HttpOnly Cookie (Final Gate)
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(isCookieSecure)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}