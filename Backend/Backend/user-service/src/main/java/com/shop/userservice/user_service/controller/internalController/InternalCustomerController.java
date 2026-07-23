package com.shop.userservice.user_service.controller.internalController;

import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.service.UserService;
import com.shop.userservice.user_service.repository.AddressRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/customer")
@Hidden
public class InternalCustomerController {

    private final UserService userService;
    private final AddressRepository addressRepository;

    public InternalCustomerController(UserService userService, AddressRepository addressRepository) {
        this.userService = userService;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/exists")
    public ResponseEntity<Void> checkCustomerExists(@RequestParam("email") String email) {
        try {
            User user = userService.findByEmail(email);
            if (user != null && user.getRoles().contains(Role.CUSTOMER) && !user.isDeleted()) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile-status")
    public ResponseEntity<Map<String, Object>> getProfileStatus(@RequestParam("email") String email) {
        try {
            User user = userService.findByEmail(email);
            if (user == null || user.isDeleted() || !user.getRoles().contains(Role.CUSTOMER)) {
                return ResponseEntity.notFound().build();
            }
            
            boolean hasAddress = addressRepository.existsByUserId(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("mobile", user.getMobile());
            response.put("hasAddress", hasAddress);
            response.put("active", user.isActive());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
