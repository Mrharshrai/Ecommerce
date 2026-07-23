package com.shop.userservice.user_service.controller.internalController;

import com.shop.userservice.user_service.dto.responseDTO.DriverProfileResponseDTO;
import com.shop.userservice.user_service.entity.DriverProfile;
import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.repository.DriverProfileRepository;
import com.shop.userservice.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/driver")
@Hidden
public class InternalDriverController {

    private final UserService userService;
    private final DriverProfileRepository driverProfileRepository;

    public InternalDriverController(UserService userService, DriverProfileRepository driverProfileRepository) {
        this.userService = userService;
        this.driverProfileRepository = driverProfileRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<DriverProfileResponseDTO> getDriverProfileByEmail(@RequestParam("email") String email) {
        try {
            User user = userService.findByEmail(email);
            if (user != null && user.getRoles().contains(Role.DRIVER) && !user.isDeleted()) {
                DriverProfile profile = driverProfileRepository.findById(user.getId()).orElse(null);
                
                DriverProfileResponseDTO dto = DriverProfileResponseDTO.builder()
                        .driverEmailId(user.getEmail())
                        .driverName(user.getName())
                        .driverPhone(profile != null ? profile.getPhoneNumber() : null)
                        .vehicleNumber(profile != null ? profile.getVehicleNumber() : null)
                        .build();
                
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
