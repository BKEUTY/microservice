package com.bkeuty.auth_service.service;

import com.bkeuty.auth_service.dto.LoginRequestDto;
import com.bkeuty.auth_service.dto.LoginResponseDto;
import com.bkeuty.auth_service.dto.RefreshTokenRequestDto;
import com.bkeuty.auth_service.dto.RefreshTokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "serverUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "clientId", "bkeuty-client");
        ReflectionTestUtils.setField(authService, "realmName", "bkeuty-realm");
        ReflectionTestUtils.setField(authService, "clientSecret", "secret123");
    }

    @Test
    void loginUser_ShouldReturnDto_WhenCredentialsAreValid() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("password123");

        Map<String, Object> mockBody = new HashMap<>();
        mockBody.put("access_token", "access_jwt");
        mockBody.put("refresh_token", "refresh_jwt");
        mockBody.put("expires_in", 3600);
        mockBody.put("refresh_expires_in", 7200);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                            .thenReturn(mockResponse);
                })) {

            LoginResponseDto response = authService.loginUser(request);

            assertNotNull(response);
            assertEquals("access_jwt", response.getAccessToken());
            assertEquals("refresh_jwt", response.getRefreshToken());
            assertEquals(3600, response.getAccessTokenExpiresIn());
            assertEquals(7200, response.getRefreshTokenExpiresIn());
        }
    }

    @Test
    void loginUser_ShouldThrowUnauthorized_WhenCredentialsAreInvalid() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
                })) {

            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.loginUser(request);
            });

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }
    }

    @Test
    void refreshToken_ShouldReturnDto_WhenTokenIsValid() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken("valid_refresh_jwt");

        Map<String, Object> mockBody = new HashMap<>();
        mockBody.put("access_token", "new_access_jwt");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                            .thenReturn(mockResponse);
                })) {

            RefreshTokenResponseDto response = authService.refreshToken(request);

            assertNotNull(response);
            assertEquals("new_access_jwt", response.getToken());
        }
    }
}
