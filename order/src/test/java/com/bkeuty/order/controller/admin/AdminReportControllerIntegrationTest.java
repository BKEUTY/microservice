package com.bkeuty.order.controller.admin;

import com.bkeuty.order.dto.admin.DashboardDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.service.admin.AdminDashboardService;
import com.bkeuty.order.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReportController.class)
class AdminReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDashboardService adminDashboardService;

    @MockitoBean
    private AuthService authService;

    // === IT_STAT_01: Super Admin xem tong doanh thu / dashboard summary ===

    @Test
    void getReportData_ShouldReturn200AndData_WhenUserIsAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder().userId("admin-001").userRole("admin").build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        DashboardDto mockDashboard = new DashboardDto();
        when(adminDashboardService.getDashboardData(any(), any(), eq("Bearer valid-admin-token")))
                .thenReturn(mockDashboard);

        mockMvc.perform(get("/api/admin/reports/data")
                        .header("Authorization", "Bearer valid-admin-token")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-31"))
                .andExpect(status().isOk());
    }

    // === IT_STAT_03: Quan ly bao mat - nguoi dung binh thuong co tinh truy cap report ===

    @Test
    void getReportData_ShouldReturn401Unauthorized_WhenUserIsRegularUser() throws Exception {
        TokenValidationResponseDto regularUserToken = TokenValidationResponseDto.builder().userId("user-123").userRole("user").build();
        when(authService.validateToken("Bearer regular-user-token")).thenReturn(regularUserToken);

        mockMvc.perform(get("/api/admin/reports/data")
                        .header("Authorization", "Bearer regular-user-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReportData_ShouldReturn401Unauthorized_WhenTokenIsMissingOrMalformed() throws Exception {
        mockMvc.perform(get("/api/admin/reports/data")
                        .header("Authorization", "malformed-token-without-bearer"))
                .andExpect(status().isUnauthorized());
    }
}
