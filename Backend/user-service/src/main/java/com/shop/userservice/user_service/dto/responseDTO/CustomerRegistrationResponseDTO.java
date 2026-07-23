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
public class CustomerRegistrationResponseDTO extends UserRegistrationResponseDTO {

    public CustomerRegistrationResponseDTO(
            Long id,
            String name,
            String email,
            String mobile,
            String userCode,
            Set<Role> roles,
            boolean isActive,
            boolean isDeleted,
            Instant createdDate,
            Instant updatedDate
    ) {
        super(id, name, email,mobile, userCode, roles, isActive, isDeleted, createdDate, updatedDate);
    }
}
