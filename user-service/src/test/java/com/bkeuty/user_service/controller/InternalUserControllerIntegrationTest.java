package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalUserController.class)
@ActiveProfiles("test")
class InternalUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getUserDetail_ShouldReturnDto() throws Exception {
        UserDetailResponseDto userDto = new UserDetailResponseDto();
        userDto.setUserId("user-123");
        userDto.setEmail("user@example.com");

        when(userService.getUserDetailById("user-123")).thenReturn(userDto);

        mockMvc.perform(get("/api/user/internal/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void getUserNames_ShouldReturnMap() throws Exception {
        List<String> ids = List.of("user-123", "user-456");
        Map<String, String> names = new HashMap<>();
        names.put("user-123", "John Doe");
        names.put("user-456", "Alice Smith");

        when(userService.getUserNames(ids)).thenReturn(names);

        mockMvc.perform(post("/api/user/internal/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['user-123']").value("John Doe"))
                .andExpect(jsonPath("$.['user-456']").value("Alice Smith"));
    }

    @Test
    void countUsers_ShouldReturnCount() throws Exception {
        when(userService.countUsersByDateRange(1000L, 2000L)).thenReturn(42L);

        mockMvc.perform(get("/api/user/internal/count")
                        .param("startDate", "1000")
                        .param("endDate", "2000"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    void listNewUsers_ShouldReturnList() throws Exception {
        UserDetailResponseDto userDto = new UserDetailResponseDto();
        userDto.setUserId("user-123");

        when(userService.getNewUsersByDateRange(1000L, 2000L)).thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/user/internal/list")
                        .param("startDate", "1000")
                        .param("endDate", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-123"));
    }

    @Test
    void updateMembershipLevel_ShouldReturnOk() throws Exception {
        doNothing().when(userService).updateMembershipLevel(eq("user-123"), eq(2), any(BigDecimal.class));

        mockMvc.perform(patch("/api/user/internal/user-123/membership-level")
                        .param("level", "2")
                        .param("totalSpending", "1500000"))
                .andExpect(status().isOk());
    }
}
