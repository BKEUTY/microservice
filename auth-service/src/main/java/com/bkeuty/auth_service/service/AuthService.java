package com.bkeuty.auth_service.service;

import com.bkeuty.auth_service.dto.LoginRequestDto;
import com.bkeuty.auth_service.dto.LoginResponseDto;
import com.bkeuty.auth_service.dto.RefreshTokenRequestDto;
import com.bkeuty.auth_service.dto.RefreshTokenResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {
    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.realm}")
    private String realmName;
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public LoginResponseDto loginUser(LoginRequestDto dto) {
        String tokenUrl = serverUrl + "/realms/" + realmName + "/protocol/openid-connect/token";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", dto.getUsername());
        map.add("password", dto.getPassword());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> body = response.getBody();
            System.out.println(body.get("refresh_token"));
            return new LoginResponseDto(
                    (String) body.get("access_token"),
                    (String) body.get("refresh_token"),
                    (Integer)  body.get("expires_in"),
                    (Integer)  body.get("refresh_expires_in")
            );
        } catch (HttpClientErrorException e) {
            // Specifically check for 401 Unauthorized
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "Authentication failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Authentication server is unreachable: " + e.getMessage());
        }
    }

    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto dto) {
        String tokenUrl = serverUrl + "/realms/" + realmName + "/protocol/openid-connect/token";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", dto.getRefreshToken());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> body = response.getBody();
            return new RefreshTokenResponseDto((String) body.get("access_token"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired, please login again");
        }
    }
}
