package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.servicecommunication.AuthServiceCommunication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthServiceCommunication authServiceCommunication;

    public AuthService(AuthServiceCommunication authServiceCommunication) {
        this.authServiceCommunication = authServiceCommunication;
    }

    public TokenValidationResponseDto validateToken(String authorizationHeader){
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            return null;
        }
        String token = authorizationHeader.substring(7);
        try {
            return authServiceCommunication.validateToken(token);
        } catch (Exception e) {
            return new TokenValidationResponseDto();
        }
    }

    public boolean isAuthenticated(String tokenHeader) {
        TokenValidationResponseDto response = validateToken(tokenHeader);
        if (response == null || response.getUserId() == null) {
            return false;
        }
        String role = response.getUserRole();
        return "ADMIN".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role);
    }

    public boolean isAdmin(String tokenHeader) {
        TokenValidationResponseDto response = validateToken(tokenHeader);
        return response != null && "ADMIN".equalsIgnoreCase(response.getUserRole());
    }
}
