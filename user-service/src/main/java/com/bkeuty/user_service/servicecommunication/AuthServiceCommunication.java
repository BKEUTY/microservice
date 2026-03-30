package com.bkeuty.user_service.servicecommunication;

import com.bkeuty.user_service.dto.auth.TokenValidationRequestDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Service
public class AuthServiceCommunication {
    private final WebClient authWebClient;

    public AuthServiceCommunication(WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    public TokenValidationResponseDto validateToken(String token) {
        TokenValidationResponseDto tokenValidationResponseDto = authWebClient.post()
                .uri("/api/auth/internal/validate-token")
                .bodyValue(new TokenValidationRequestDto(token)).retrieve().bodyToMono(TokenValidationResponseDto.class)
                .onErrorReturn(new TokenValidationResponseDto()).block();
        return tokenValidationResponseDto;

    }
}
