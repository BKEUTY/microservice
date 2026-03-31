package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.servicecommunication.AuthServiceCommunication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthServiceCommunication authServiceCommunication;
    public AuthService(AuthServiceCommunication authServiceCommunication) {
        this.authServiceCommunication = authServiceCommunication;
    }

    public TokenValidationResponseDto validateToken(String authorizationHeader){
        if(authorizationHeader == null){
            return null;
        }
        String token = authorizationHeader.substring(7);
        return authServiceCommunication.validateToken(token);
    }
}
