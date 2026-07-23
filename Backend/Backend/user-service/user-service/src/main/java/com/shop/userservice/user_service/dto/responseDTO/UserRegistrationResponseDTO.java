package com.shop.userservice.user_service.dto.responseDTO;

import com.shop.userservice.user_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String userCode;
    private Set<Role> roles;
    private boolean isActive;
    private boolean isDeleted;
    private Instant createdDate;
    private Instant updatedDate;
}

