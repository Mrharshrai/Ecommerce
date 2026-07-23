package com.shop.userservice.user_service.dto.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.userservice.user_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOwnProfileResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private boolean mobileVerified;
    private boolean emailVerified;

    @JsonInclude(JsonInclude.Include.NON_NULL) // Applied ONLY to this field
    private String vehicleNumber;
}
