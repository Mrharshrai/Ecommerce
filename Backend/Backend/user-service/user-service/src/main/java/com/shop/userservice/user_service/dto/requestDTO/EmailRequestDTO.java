package com.shop.userservice.user_service.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDTO {
    private String toEmail;
    private String subject;
    private String body;
}
