package com.bkeuty.order.service.auth;

import com.bkeuty.order.dto.auth.TokenValidationRequestDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Service
public class AuthService {
    private final WebClient authWebClient;

    public AuthService(WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    public TokenValidationResponseDto validateToken(String authorizationHeader)
    {
        if(authorizationHeader == null){
            return null;
        }
        String token = authorizationHeader.substring(7);
        TokenValidationResponseDto tokenValidationResponseDto = authWebClient.post().uri("/api/auth/internal/validate-token")
                .bodyValue(new TokenValidationRequestDto(token)).retrieve().bodyToMono(TokenValidationResponseDto.class).onErrorReturn(new TokenValidationResponseDto()).block();

        return tokenValidationResponseDto;

    }
}
