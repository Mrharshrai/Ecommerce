package com.shop.userservice.user_service.mapper;

import com.shop.userservice.user_service.dto.responseDTO.CustomerRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.DriverRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserRegistrationResponseDTO;
import com.shop.userservice.user_service.entity.DriverProfile;
import com.shop.userservice.user_service.entity.Role;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.repository.DriverProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    @Autowired
    private DriverProfileRepository driverProfileRepository;

    // -----------------------------
    // Generic method used everywhere
    // -----------------------------
    public UserRegistrationResponseDTO toDTO(User user) {

        // DRIVER
        if (user.getRoles().contains(Role.DRIVER)) {
            DriverProfile profile =
                    driverProfileRepository.findById(user.getId()).orElse(null);

            return new DriverRegistrationResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getUserCode(),
                    user.getRoles(),
                    user.isActive(),
                    user.isDeleted(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    profile != null ? profile.getPhoneNumber() : null,
                    profile != null ? profile.getVehicleNumber() : null
            );
        }

        // CUSTOMER
        if (user.getRoles().contains(Role.CUSTOMER)) {
            return new CustomerRegistrationResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getMobile(),
                    user.getUserCode(),
                    user.getRoles(),
                    user.isActive(),
                    user.isDeleted(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }

        // ADMIN or OTHER
        return new UserRegistrationResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getUserCode(),
                user.getRoles(),
                user.isActive(),
                user.isDeleted(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Optional dedicated mapping (if needed anywhere)
    public CustomerRegistrationResponseDTO toCustomerDTO(User user) {
        return new CustomerRegistrationResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getUserCode(),
                user.getRoles(),
                user.isActive(),
                user.isDeleted(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public DriverRegistrationResponseDTO toDriverDTO(User user, DriverProfile profile) {
        return new DriverRegistrationResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserCode(),
                user.getRoles(),
                user.isActive(),
                user.isDeleted(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                profile.getPhoneNumber(),
                profile.getVehicleNumber()
        );
    }
}
