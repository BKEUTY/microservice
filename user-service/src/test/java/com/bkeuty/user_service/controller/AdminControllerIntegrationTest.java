package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.service.AuthService;
import com.bkeuty.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    private TokenValidationResponseDto adminToken;

    @BeforeEach
    void setUp() {
        adminToken = new TokenValidationResponseDto();
        adminToken.setUserId("admin-123");
        adminToken.setUserRole("admin");
    }

    @Test
    void getListUserDetail_ShouldReturnUsers_WhenAdmin() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(adminToken);

        UserDetailResponseDto userDto = new UserDetailResponseDto();
        userDto.setUserId("user-123");
        when(userService.getListUserDetail("user")).thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/admin/user")
                        .header("Authorization", "Bearer valid-token")
                        .param("role", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-123"));
    }

    @Test
    void getListUserDetail_ShouldReturnUnauthorized_WhenNotAdmin() throws Exception {
        TokenValidationResponseDto userToken = new TokenValidationResponseDto();
        userToken.setUserId("user-123");
        userToken.setUserRole("user");

        when(authService.validateToken("Bearer user-token")).thenReturn(userToken);

        mockMvc.perform(get("/api/admin/user")
                        .header("Authorization", "Bearer user-token")
                        .param("role", "user"))
                .andExpect(status().isUnauthorized());
    }
}
