package com.bkeuty.review_service.service;

import com.bkeuty.review_service.dto.auth.TokenValidationRequestDto;
import com.bkeuty.review_service.dto.auth.TokenValidationResponseDto;
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
        if (authorizationHeader == null) {
            return new TokenValidationResponseDto();
        }
        String token = authorizationHeader.substring(7);
        TokenValidationResponseDto tokenValidationResponseDto = authWebClient.post()
                .uri("/api/auth/internal/validate-token")
                .bodyValue(new TokenValidationRequestDto(token)).retrieve().bodyToMono(TokenValidationResponseDto.class)
                .onErrorReturn(new TokenValidationResponseDto()).block();

        return tokenValidationResponseDto;

    }
}
