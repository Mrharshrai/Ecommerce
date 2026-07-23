package com.shop.userservice.user_service.dto.responseDTO;

import com.shop.userservice.user_service.entity.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DriverRegistrationResponseDTO extends UserRegistrationResponseDTO {

    private String phoneNumber;
    private String vehicleNumber;

    public DriverRegistrationResponseDTO(
            Long id,
            String name,
            String email,
            String userCode,
            Set<Role> roles,
            boolean isActive,
            boolean isDeleted,
            Instant createdDate,
            Instant updatedDate,
            String phoneNumber,
            String vehicleNumber
    ) {
        super(id, name, email,null, userCode, roles,isActive, isDeleted,createdDate,updatedDate);
        this.phoneNumber = phoneNumber;
        this.vehicleNumber = vehicleNumber;
    }
}
