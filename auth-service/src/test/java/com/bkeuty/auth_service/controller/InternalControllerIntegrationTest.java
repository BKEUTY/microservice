package com.bkeuty.auth_service.controller;

import com.bkeuty.auth_service.dto.TokenValidationRequestDto;
import com.bkeuty.auth_service.dto.TokenValidationResponseDto;
import com.bkeuty.auth_service.jwtUtil.AccessTokenValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalController.class)
@ActiveProfiles("test")
class InternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccessTokenValidator accessTokenValidator;

    @Test
    void validateAccessToken_ShouldReturnResponse_WhenValid() throws Exception {
        TokenValidationRequestDto request = new TokenValidationRequestDto();
        request.setToken("valid-jwt");

        TokenValidationResponseDto response = TokenValidationResponseDto.builder()
                .userId("user-123")
                .userRole("user")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(accessTokenValidator.validate("valid-jwt")).thenReturn(response);

        mockMvc.perform(post("/api/auth/internal/validate-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.userRole").value("user"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void validateAccessToken_ShouldReturnBadRequest_WhenInvalid() throws Exception {
        TokenValidationRequestDto request = new TokenValidationRequestDto();
        request.setToken("invalid-jwt");

        when(accessTokenValidator.validate("invalid-jwt")).thenReturn(null);

        mockMvc.perform(post("/api/auth/internal/validate-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
