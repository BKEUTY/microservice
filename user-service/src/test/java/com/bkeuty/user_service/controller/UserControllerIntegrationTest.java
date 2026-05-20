package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.AddressDto;
import com.bkeuty.user_service.dto.UpdateUserDto;
import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.service.AuthService;
import com.bkeuty.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    private TokenValidationResponseDto userToken;

    @BeforeEach
    void setUp() {
        userToken = new TokenValidationResponseDto();
        userToken.setUserId("user-123");
        userToken.setUserRole("user");
    }

    @Test
    void getHealthCheck_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/user/healthcheck"))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_ShouldReturnProfile_WhenAuthorized() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        UserDetailResponseDto profile = new UserDetailResponseDto();
        profile.setUserId("user-123");
        profile.setEmail("user@example.com");
        when(userService.getUserProfile(userToken)).thenReturn(profile);

        mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void getUser_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        when(authService.validateToken("Bearer invalid-token")).thenReturn(null);

        mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_ShouldReturnUpdatedProfile_WhenAuthorized() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstname("John");

        when(userService.updateUserProfile(any(UpdateUserDto.class), eq(userToken))).thenReturn(updateDto);

        mockMvc.perform(put("/api/user")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John"));
    }

    @Test
    void getAddress_ShouldReturnList_WhenAuthorized() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        AddressDto address = new AddressDto();
        address.setAddress("123 Main St");
        when(userService.getAddresses(userToken)).thenReturn(List.of(address));

        mockMvc.perform(get("/api/user/address")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value("123 Main St"));
    }

    @Test
    void addAddress_ShouldReturnTrue_WhenAuthorized() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        AddressDto address = new AddressDto();
        address.setAddress("123 Main St");
        when(userService.addNewAddress(eq(userToken), any(AddressDto.class))).thenReturn(true);

        mockMvc.perform(post("/api/user/address")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void deleteAddress_ShouldReturnTrue_WhenAuthorized() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        AddressDto address = new AddressDto();
        address.setAddress("123 Main St");
        when(userService.deleteAddress(any(AddressDto.class), eq(userToken))).thenReturn(true);

        mockMvc.perform(delete("/api/user/address")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
