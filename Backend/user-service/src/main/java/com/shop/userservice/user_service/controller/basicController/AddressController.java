package com.shop.userservice.user_service.controller.basicController;

import com.shop.userservice.user_service.dto.requestDTO.AddressDTO;
import com.shop.userservice.user_service.dto.responseDTO.AddressResponseDTO;
import com.shop.userservice.user_service.dto.updateDTO.UpdateAddressDTO;
import com.shop.userservice.user_service.entity.ActivityType;
import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.service.AddressService;
import com.shop.userservice.user_service.service.UserActivityService;
import com.shop.userservice.user_service.service.UserService;
import com.shop.userservice.user_service.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Address Management", description = "APIs for managing user addresses")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;
    private final UserActivityService activityService;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public AddressController(AddressService addressService, UserService userService, UserActivityService activityService) {
        this.addressService = addressService;
        this.userService = userService;
        this.activityService = activityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add new address", description = "Customer can add a new delivery address")
    public ResponseEntity<AddressResponseDTO> addAddress(@Valid @RequestBody AddressDTO addressDTO, Authentication authentication,
                                                         HttpServletRequest httpRequest){
        User user = userService.findByEmail(authentication.getName());
        AddressResponseDTO response = addressService.addAddress(user.getId(), addressDTO);

        // Activity Logging
        try {
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    Role.CUSTOMER.name(),
                    ActivityType.ADD_ADDRESS, // Ensure this matches your ActivityType enum
                    "Customer added a new delivery address: ",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log add address activity: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update address", description = "Customer can update their existing address")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateAddressDTO addressDTO,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User user = userService.findByEmail(authentication.getName());
        AddressResponseDTO response = addressService.updateAddress(user.getId(), id, addressDTO);

        // Activity Logging
        try {
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    Role.CUSTOMER.name(),
                    ActivityType.UPDATE_ADDRESS,
                    "Customer updated their delivery address details (Address ID: " + id + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log update address activity: {}", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get all addresses", description = "Customer can view all their saved addresses")
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<AddressResponseDTO> addresses = addressService.getAllAddresses(user.getId());
        return ResponseEntity.ok(addresses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Delete address", description = "Customer can delete their address")
    public ResponseEntity<String> deleteAddress(
            @PathVariable("id") Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User user = userService.findByEmail(authentication.getName());
        addressService.deleteAddress(user.getId(), id);

        // Activity Logging
        try {
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    Role.CUSTOMER.name(),
                    ActivityType.DELETE_ADDRESS,
                    "Customer permanently removed an address profile (Address ID: " + id + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log delete address activity: {}", e.getMessage());
        }
        return ResponseEntity.ok("Address deleted successfully");
    }

    @PutMapping("/{id}/set-default")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Set default address", description = "Customer can set an address as default for delivery")
    public ResponseEntity<AddressResponseDTO> setDefaultAddress(
            @PathVariable("id") Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User user = userService.findByEmail(authentication.getName());
        AddressResponseDTO response = addressService.setDefaultAddress(user.getId(), id);

        // Activity Logging
        try {
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    Role.CUSTOMER.name(),
                    ActivityType.SET_DEFAULT_ADDRESS, // Ensure this matches your ActivityType enum
                    "Customer changed their default primary shipping address to Address ID: " + id,
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log set default address activity: {}", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
