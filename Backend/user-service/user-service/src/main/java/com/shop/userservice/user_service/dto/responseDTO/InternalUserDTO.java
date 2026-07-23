package com.shop.userservice.user_service.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InternalUserDTO {
    
    private Long id;
    private String name;
    private String email;
    private String userCode;
    private Set<String> roles;
    private boolean active;
    private boolean deleted;
    private boolean emailVerified;
    private boolean mobileVerified;
}
