package com.shop.userservice.user_service.controller.internalController;

import com.shop.userservice.user_service.dto.responseDTO.InternalUserDTO;
import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.repository.AddressRepository;
import com.shop.userservice.user_service.security.JwtUtil;
import com.shop.userservice.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/users")
@Hidden
public class InternalUserController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final AddressRepository addressRepository;

    public InternalUserController(UserService userService, JwtUtil jwtUtil, AddressRepository addressRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") Long userId) {
        try {
            User user = userService.findById(userId);
            
            InternalUserDTO dto = InternalUserDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .userCode(user.getUserCode())
                    .roles(user.getRoles().stream()
                            .map(Role::name)
                            .collect(Collectors.toSet()))
                    .active(user.isActive())
                    .deleted(user.isDeleted())
                    .emailVerified(user.isEmailVerified())
                    .mobileVerified(user.isMobileVerified())
                    .build();
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String requiredRole = request.get("role");
        
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        
        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Invalid token");
                return ResponseEntity.ok(response);
            }
            
            // Extract username and roles
            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);
            
            // Check if user exists and is active
            User user = userService.findByEmail(username);
            
            if (!user.isActive() || user.isDeleted()) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "User is not active");
                return ResponseEntity.ok(response);
            }
            
            // Check role if required
            boolean hasRole = true;
            if (requiredRole != null && !requiredRole.isEmpty()) {
                hasRole = roles.stream()
                        .anyMatch(role -> role.equals("ROLE_" + requiredRole) || role.equals(requiredRole));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("roles", roles);
            response.put("hasRequiredRole", hasRole);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token validation failed");
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Check if user can place an order
     * Requirements: User must be active, not deleted, email verified, and have at least one address
     */
    @GetMapping("/{userId}/can-place-order")
    public ResponseEntity<?> canPlaceOrder(@PathVariable("userId") Long userId) {
        try {
            User user = userService.findById(userId);
            
            // Check if user has at least one address
            long addressCount = addressRepository.findByUserId(userId).size();
            boolean hasAddress = addressCount > 0;
            
            // Check if user can place order
            boolean canPlaceOrder = user.isActive() 
                    && !user.isDeleted() 
                    && user.isEmailVerified()
                    && hasAddress;
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("canPlaceOrder", canPlaceOrder);
            response.put("emailVerified", user.isEmailVerified());
            response.put("active", user.isActive());
            response.put("deleted", user.isDeleted());
            response.put("hasAddress", hasAddress);
            response.put("addressCount", addressCount);
            
            if (!canPlaceOrder) {
                if (!user.isEmailVerified()) {
                    response.put("reason", "Email not verified");
                } else if (!user.isActive()) {
                    response.put("reason", "Account is deactivated");
                } else if (user.isDeleted()) {
                    response.put("reason", "Account is deleted");
                } else if (!hasAddress) {
                    response.put("reason", "No delivery address added");
                }
            } else {
                response.put("reason", "Eligible to place order");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("canPlaceOrder", false);
            response.put("reason", "User not found");
            return ResponseEntity.status(404).body(response);
        }
    }
}
