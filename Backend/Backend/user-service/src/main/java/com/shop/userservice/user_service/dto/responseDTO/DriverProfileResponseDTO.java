package com.shop.userservice.user_service.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileResponseDTO {
    private String driverEmailId;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
}
