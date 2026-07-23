package com.shop.userservice.user_service.dto.updateDTO;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmployeeUpdateDTO {
    @Pattern(
            regexp = "^.{8,}$",
            message = "password must be at least 8 characters"
    )
    private String password; // driver/Admin can update only password per your rules
}


