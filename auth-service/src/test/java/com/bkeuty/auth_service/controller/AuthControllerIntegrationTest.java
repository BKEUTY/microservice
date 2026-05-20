package com.bkeuty.auth_service.controller;

import com.bkeuty.auth_service.dto.*;
import com.bkeuty.auth_service.jwtUtil.AccessTokenValidator;
import com.bkeuty.auth_service.service.AuthService;
import com.bkeuty.auth_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AccessTokenValidator accessTokenValidator;

    @Test
    void getHealthCheck_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/auth/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("Healthcheck"));
    }

    @Test
    void registerUser_ShouldReturnCreated() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("testuser");

        RegisterResponseDto response = new RegisterResponseDto();
        response.setUsername("testuser");

        when(userService.registerUser(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_ShouldReturnResponseAndSetCookie_WhenValid() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setClientType("user");

        LoginResponseDto loginResponse = new LoginResponseDto();
        loginResponse.setAccessToken("access-jwt");
        loginResponse.setRefreshToken("refresh-jwt");
        loginResponse.setRefreshTokenExpiresIn(7200);

        when(authService.loginUser(any(LoginRequestDto.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("wrong");

        when(authService.loginUser(any(LoginRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_ShouldReturnResponse_WhenValidCookie() throws Exception {
        RefreshTokenResponseDto response = new RefreshTokenResponseDto();
        response.setToken("new-access-jwt");

        when(authService.refreshToken(any(RefreshTokenRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("user_refreshToken", "refresh-jwt"))
                        .header("X-Client-Type", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-jwt"));
    }

    @Test
    void logout_ShouldClearCookies() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("X-Client-Type", "user"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));
    }
}
