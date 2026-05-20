package com.bkeuty.order.controller.admin;

import com.bkeuty.order.dto.admin.AdminRefundOrderDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.enums.RefundStatus;
import com.bkeuty.order.service.admin.AdminRefundOrderService;
import com.bkeuty.order.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRefundOrderController.class)
@ActiveProfiles("test")
class AdminRefundOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminRefundOrderService adminRefundOrderService;

    @MockitoBean
    private AuthService authService;

    private AdminRefundOrderDto refundDto;

    @BeforeEach
    void setUp() {
        refundDto = AdminRefundOrderDto.builder()
                .refundOrderId(100)
                .orderId(1)
                .userId("user-123")
                .userName("Nguyen Van A")
                .total(new BigDecimal("150000"))
                .status(RefundStatus.PENDING)
                .phoneNumber("0909090909")
                .note("Loi sp")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllRefundOrders_ShouldReturnOk_WhenAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder()
                .userId("admin-001")
                .userRole("admin")
                .build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        Page<AdminRefundOrderDto> page = new PageImpl<>(List.of(refundDto));
        when(adminRefundOrderService.getAllRefundOrders(any(PageRequest.class), eq("PENDING"))).thenReturn(page);

        mockMvc.perform(get("/api/admin/refund-order")
                        .header("Authorization", "Bearer valid-admin-token")
                        .param("page", "1")
                        .param("size", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].refundOrderId").value(100))
                .andExpect(jsonPath("$.content[0].userName").value("Nguyen Van A"));
    }

    @Test
    void getAllRefundOrders_ShouldReturn401_WhenNotAdmin() throws Exception {
        TokenValidationResponseDto userToken = TokenValidationResponseDto.builder()
                .userId("user-123")
                .userRole("user")
                .build();
        when(authService.validateToken("Bearer user-token")).thenReturn(userToken);

        mockMvc.perform(get("/api/admin/refund-order")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRefundOrderById_ShouldReturnOk_WhenAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder()
                .userId("admin-001")
                .userRole("admin")
                .build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        when(adminRefundOrderService.getRefundOrderById(100)).thenReturn(refundDto);

        mockMvc.perform(get("/api/admin/refund-order/100")
                        .header("Authorization", "Bearer valid-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundOrderId").value(100));
    }

    @Test
    void approveRefundOrder_ShouldReturnOk_WhenAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder()
                .userId("admin-001")
                .userRole("admin")
                .build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        refundDto.setStatus(RefundStatus.APPROVED);
        when(adminRefundOrderService.approveRefundOrder(100)).thenReturn(refundDto);

        mockMvc.perform(put("/api/admin/refund-order/100/approve")
                        .header("Authorization", "Bearer valid-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void rejectRefundOrder_ShouldReturnOk_WhenAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder()
                .userId("admin-001")
                .userRole("admin")
                .build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        refundDto.setStatus(RefundStatus.REJECTED);
        when(adminRefundOrderService.rejectRefundOrder(100)).thenReturn(refundDto);

        mockMvc.perform(put("/api/admin/refund-order/100/reject")
                        .header("Authorization", "Bearer valid-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void completeRefundOrder_ShouldReturnOk_WhenAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder()
                .userId("admin-001")
                .userRole("admin")
                .build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        refundDto.setStatus(RefundStatus.DELIVERED);
        when(adminRefundOrderService.completeRefundOrder(100)).thenReturn(refundDto);

        mockMvc.perform(put("/api/admin/refund-order/100/complete")
                        .header("Authorization", "Bearer valid-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void processMoneyRefund_ShouldReturnOk_WhenAdmin() throws Exception {
        TokenValidationResponseDto adminToken = TokenValidationResponseDto.builder()
                .userId("admin-001")
                .userRole("admin")
                .build();
        when(authService.validateToken("Bearer valid-admin-token")).thenReturn(adminToken);

        when(adminRefundOrderService.processMoneyRefund(100)).thenReturn(refundDto);

        mockMvc.perform(post("/api/admin/refund-order/100/process-refund")
                        .header("Authorization", "Bearer valid-admin-token"))
                .andExpect(status().isOk());
    }
}
