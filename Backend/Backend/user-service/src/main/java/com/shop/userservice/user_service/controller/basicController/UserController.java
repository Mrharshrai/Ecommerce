package com.shop.userservice.user_service.controller.basicController;

import com.shop.userservice.user_service.dto.requestDTO.CustomerRegistrationDTO;
import com.shop.userservice.user_service.dto.requestDTO.DriverRegistrationDTO;
import com.shop.userservice.user_service.dto.responseDTO.CustomerRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.DriverRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserOwnProfileResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.updateDTO.CustomerUpdateDTO;
import com.shop.userservice.user_service.dto.updateDTO.DriverDetailsUpdateDTO;
import com.shop.userservice.user_service.dto.updateDTO.EmployeeUpdateDTO;
import com.shop.userservice.user_service.entity.ActivityType;
import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.service.UserActivityService;
import com.shop.userservice.user_service.service.UserService;
import com.shop.userservice.user_service.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;
    private final UserActivityService activityService;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserController(UserService userService, UserActivityService activityService) {
        this.userService = userService;
        this.activityService = activityService;
    }

    // 1. Anyone can register as CUSTOMER
    @PostMapping("/register/customer")
    public ResponseEntity<CustomerRegistrationResponseDTO> registerCustomer(@RequestBody @Valid CustomerRegistrationDTO dto , HttpServletRequest httpRequest) {
        CustomerRegistrationResponseDTO response = (CustomerRegistrationResponseDTO) userService.createCustomerAndGetDTO(dto);
        // Activity Logging
        try {
            activityService.logActivity(
                    response.getId(),
                    response.getEmail(),
                    Role.CUSTOMER.name(),
                    ActivityType.REGISTRATION, // Replace with your specific ActivityType enum
                    "New customer registered successfully" + response.getEmail(),
                    httpRequest
            );
        } catch (Exception e) {
            // Log warning but don't fail the registration endpoint
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // View own profile
    @GetMapping("/profile")
    public ResponseEntity<UserOwnProfileResponseDTO> getOwnProfile(Authentication authentication) {
        String email = authentication.getName();
        UserOwnProfileResponseDTO response = userService.myProfile(email);
        return ResponseEntity.ok(response);
    }

    // 2. Only ADMIN can register DRIVERS
    @Hidden
    @PostMapping("/register/driver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverRegistrationResponseDTO> registerDriver(@RequestBody @Valid DriverRegistrationDTO dto,
                                                                        Authentication authentication,
                                                                        HttpServletRequest httpRequest) {
        User createdDriver = userService.createDriver(dto, httpRequest);
        // Get full DTO from service (includes DriverProfile)
        DriverRegistrationResponseDTO response = (DriverRegistrationResponseDTO) userService.getUserById(createdDriver.getId());

        // Log Admin Action: Track that the admin initiated this registration
        try {
            String adminEmail = authentication.getName();
            User admin = userService.findByEmail(adminEmail);
            activityService.logActivity(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getRoles().iterator().next().name(),
                    ActivityType.REGISTER_DRIVER,
                    "Admin"+ adminEmail +"registered a new driver with ID: " + createdDriver.getId() +createdDriver.getEmail(),
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log admin registration activity: {}", e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 3. Only ADMIN can view all users
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserRegistrationResponseDTO>> getAllUsers(Pageable pageable) {
        Page<UserRegistrationResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    // 4. get a user by id
    @GetMapping("/get/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRegistrationResponseDTO> getUserById(@PathVariable("id") Long id) {
        UserRegistrationResponseDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    // 5. Update customer details
    // Customer can update their own> name, email, mobile number, not anything else
    @PutMapping("/updateCustomer/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserRegistrationResponseDTO> updateCustomer(@PathVariable("id") Long id, Authentication authentication,
                                                                      @RequestBody @Valid CustomerUpdateDTO updatedUser, HttpServletRequest httpRequest) {

        // authentication.getName() returns email
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);

        if (!currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You can update only your own account.");
        }
        UserRegistrationResponseDTO response = userService.updateCustomer(id, updatedUser);
        // Activity Logging
        try {
            activityService.logActivity(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getRoles().iterator().next().name(),
                    ActivityType.PROFILE_UPDATE,
                    "Customer updated their own profile details (Name/Email/Mobile)",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log customer update activity: {}", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 6. Driver/Admin/Customer can update their own> password only, not anything else
    @PutMapping("/updatePassword/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER','CUSTOMER')")
    public ResponseEntity<UserRegistrationResponseDTO> updatePassword(@PathVariable("id") Long id, Authentication authentication,
                                                                      @RequestBody @Valid EmployeeUpdateDTO updatedUser, HttpServletRequest httpRequest) {
        // authentication.getName() returns email
        String email = authentication.getName();

        User currentUser = userService.findByEmail(email);
        if (!currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You can update only your own account's password.");
        }
        UserRegistrationResponseDTO response = userService.updatePassword(id, updatedUser);

        // Activity Logging
        try {
            activityService.logActivity(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getRoles().iterator().next().name(),
                    ActivityType.UPDATE_PASSWORD,
                    "User changed their account password",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log password update activity: {}", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 7. Admin has right to update driver's name, email, vehicle number, phone number only
    @PutMapping("/updateDriverDetails/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverRegistrationResponseDTO> updateDriverDetails(@PathVariable("id") Long id, @RequestBody @Valid DriverDetailsUpdateDTO dto,
                                                                             HttpServletRequest httpRequest, Authentication authentication) {
        DriverRegistrationResponseDTO updated = userService.updateDriverDetails(id, dto,httpRequest);

        // Activity Logging
        try {
            String adminEmail = authentication.getName();
            User admin = userService.findByEmail(adminEmail);

            // Build a string showing what fields the admin requested to change
            java.util.StringJoiner updatedFields = new java.util.StringJoiner(", ");
            if (dto.getName() != null) updatedFields.add("Name");
            if (dto.getEmail() != null) updatedFields.add("Email");
            if (dto.getPhoneNumber() != null) updatedFields.add("Phone Number");
            if (dto.getVehicleNumber() != null) updatedFields.add("Vehicle Number");

            String description = String.format("Admin "+adminEmail+" updated driver profile (ID: %d). Fields modified: [%s]",
                    id, updatedFields.length() > 0 ? updatedFields.toString() : "None");

            activityService.logActivity(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getRoles().iterator().next().name(),
                    ActivityType.UPDATE_DRIVER,
                    description,
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log admin update driver activity: {}", e.getMessage());
        }

        return ResponseEntity.ok(updated);
    }

    // 8. customer can delete their own account permanently and Admin  can delete any customer's account permanently
    @DeleteMapping("/deleteAccount/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> deleteAccount(@PathVariable("id") Long id, @RequestBody @Valid String reason, Authentication authentication, HttpServletRequest httpRequest) {
        // authentication.getName() returns email
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);

        if (currentUser.getRoles().contains(Role.CUSTOMER)) {
            if (!currentUser.getId().equals(id)) {
                throw new AccessDeniedException("Customers can delete only their own account.");
            }
        }
        // ADMIN deleting ANY customer
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            userService.ensureTargetIsCustomer(id);  // prevents deleting admin/driver
        }
        userService.deleteUser(id);

        // Activity Logging
        try {
            // Clean up the reason string (strip accidental JSON quotes or trailing whitespaces)
            String cleanReason = reason.replace("\"", "").trim();

            String description;
            if (currentUser.getRoles().contains(Role.ADMIN)) {
                description = String.format("Admin "+currentUser+" permanently deleted customer account (Target ID: %d). Reason: %s", id, cleanReason);
            } else {
                description = String.format("Customer permanently deleted their own account. Reason: %s", cleanReason);
            }
            activityService.logActivity(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getRoles().iterator().next().name(),
                    ActivityType.DELETE_ACCOUNT,
                    description,
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log account deletion activity: {}", e.getMessage());
        }
        return ResponseEntity.ok("Customer's Account deleted successfully");
    }

    // 9. Only ADMIN can SOFT delete a driver's  account,
    @DeleteMapping("/softDeleteEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEmployee(@PathVariable("id") Long id,@RequestBody @Valid String reason,Authentication authentication,
                                                 HttpServletRequest httpRequest) {
        userService.softDeleteUser(id, httpRequest);

        // Activity Logging
        try {
            String cleanReason = reason.replace("\"", "").trim();
            String adminEmail = authentication.getName();
            User admin = userService.findByEmail(adminEmail);
            activityService.logActivity(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getRoles().iterator().next().name(),
                    ActivityType.SOFT_DELETE_USER,
                    "Admin "+ adminEmail +" soft deleted employee account (Target ID: " + id + ")"+"(Reason: "+ cleanReason + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log employee soft delete activity: {}", e.getMessage());
        }

        return ResponseEntity.ok("User soft deleted successfully");
    }

    // 9.1 Only ADMIN can restore a driver's  account
    @PutMapping("/restoreEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restoreEmployee(@PathVariable("id") Long id, Authentication authentication,
                                                  HttpServletRequest httpRequest) {
        userService.restoreUser(id, httpRequest);

        // Activity Logging
        try {
            String adminEmail = authentication.getName();
            User admin = userService.findByEmail(adminEmail);
            activityService.logActivity(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getRoles().iterator().next().name(),
                    ActivityType.RESTORE_USER,
                    "Admin "+ adminEmail +"  restored employee account (Target ID: " + id + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log employee restore activity: {}", e.getMessage());
        }
        return ResponseEntity.ok("User restored successfully");
    }
    
    // ==================== ADMIN CUSTOMER/DRIVER MANAGEMENT ====================
    
    // 10. Admin can get list of active customers
    @GetMapping("/customers/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserRegistrationResponseDTO>> getActiveCustomers(Pageable pageable) {
        Page<UserRegistrationResponseDTO> activeCustomers = userService.getActiveCustomers(pageable);
        return ResponseEntity.ok(activeCustomers);
    }
    
    // 11. Admin can get one customer details
    @GetMapping("/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRegistrationResponseDTO> getCustomerDetails(@PathVariable("id") Long id) {
        UserRegistrationResponseDTO customer = userService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }
    
    // 12. Admin can activate customer/driver's account
    @PutMapping("/activate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(@PathVariable("id") Long id,
                                               Authentication authentication,
                                               HttpServletRequest httpRequest) {
        userService.activateUser(id, httpRequest);
        // Activity Logging
        try {
            String adminEmail = authentication.getName();
            User admin = userService.findByEmail(adminEmail);
            activityService.logActivity(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getRoles().iterator().next().name(),
                    ActivityType.ACTIVATE_USER,
                    "Admin "+ adminEmail +" activated user account (Target ID: " + id + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log user activation activity: {}", e.getMessage());
        }
        return ResponseEntity.ok("User account activated successfully");
    }
    
    // 13. Admin can deactivate customer/driver account
    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@PathVariable("id") Long id, @RequestBody @Valid String reason,
                                                 Authentication authentication,
                                                 HttpServletRequest httpRequest) {
        userService.deactivateUser(id, httpRequest);

        // Activity Logging
        try {
            String cleanReason = reason.replace("\"", "").trim();
            String adminEmail = authentication.getName();
            User admin = userService.findByEmail(adminEmail);
            activityService.logActivity(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getRoles().iterator().next().name(),
                    ActivityType.DEACTIVATE_USER,
                    "Admin "+ adminEmail + " deactivated user account (Target ID: " + id + ")"+"(Reason: "+ cleanReason + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log user deactivation activity: {}", e.getMessage());
        }
        return ResponseEntity.ok("User account deactivated successfully");
    }
    
    // ==================== CUSTOMER SELF-SERVICE ====================
    
    // 14. Customer can deactivate own account
    @PutMapping("/customer/deactivate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> deactivateOwnAccount(@RequestBody @Valid String reason,Authentication authentication, HttpServletRequest httpRequest) {
        String email = authentication.getName();
        userService.deactivateOwnAccount(email);

        // Activity Logging
        try {
            String cleanReason = reason.replace("\"", "").trim();
            User currentUser = userService.findByEmail(email);
            activityService.logActivity(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getRoles().iterator().next().name(),
                    ActivityType.DEACTIVATE_OWN_ACCOUNT,
                    "Customer self-deactivated their own account"+"(Reason: "+ cleanReason + ")",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log customer self-deactivation activity: {}", e.getMessage());
        }
        return ResponseEntity.ok("Your account has been deactivated successfully");
    }
}
