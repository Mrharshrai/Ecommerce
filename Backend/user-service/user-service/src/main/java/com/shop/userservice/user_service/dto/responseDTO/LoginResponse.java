package com.shop.userservice.user_service.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private UserRegistrationResponseDTO user;

    // Constructor for backward compatibility
    public LoginResponse(String token, UserRegistrationResponseDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserRegistrationResponseDTO getUser() {
        return user;
    }

    public void setUser(UserRegistrationResponseDTO user) {
        this.user = user;
    }
}
