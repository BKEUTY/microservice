package com.bkeuty.product.service.authservice;

import com.bkeuty.product.dto.auth.TokenValidationRequestDto;
import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {
    private final WebClient authWebClient;

    public AuthService(@Qualifier("authWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    public TokenValidationResponseDto validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new TokenValidationResponseDto();
        }
        String token = authorizationHeader.substring(7);
        try {
            return authWebClient.post()
                    .uri("/api/auth/internal/validate-token")
                    .bodyValue(new TokenValidationRequestDto(token))
                    .retrieve()
                    .bodyToMono(TokenValidationResponseDto.class)
                    .onErrorReturn(new TokenValidationResponseDto())
                    .block();
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
