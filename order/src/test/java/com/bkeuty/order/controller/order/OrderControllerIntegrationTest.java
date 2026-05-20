package com.bkeuty.order.controller.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.order.CreateRefundOrderRequestDto;
import com.bkeuty.order.dto.order.OrderResponseDto;
import com.bkeuty.order.dto.order.PlaceOrderRequestDto;
import com.bkeuty.order.dto.order.UserRefundOrderDto;
import com.bkeuty.order.dto.shipping.GetShippingOrderStatusRequest;
import com.bkeuty.order.dto.shipping.GetShippingOrderStatusResponseDto;
import com.bkeuty.order.entity.RefundOrder;
import com.bkeuty.order.enums.RefundStatus;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.order.OrderService;
import com.bkeuty.order.service.order.RefundOrderService;
import com.bkeuty.order.service.shipping.ShippingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
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

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ShippingService shippingService;

    @MockitoBean
    private RefundOrderService refundOrderService;

    private TokenValidationResponseDto userToken;

    @BeforeEach
    void setUp() {
        userToken = TokenValidationResponseDto.builder()
                .userId("user-123")
                .userRole("user")
                .build();
    }

    @Test
    void getHealthCheck_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/order/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("OK"));
    }

    @Test
    void findOrderByUserId_ShouldReturnPage_WhenUser() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        Page<OrderResponseDto> page = new PageImpl<>(List.of(new OrderResponseDto()));
        when(orderService.getListOrders(eq("user-123"), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/order/history")
                        .header("Authorization", "Bearer valid-token")
                        .param("page", "1")
                        .param("size", "10")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk());
    }

    @Test
    void placeOrder_ShouldReturnResult_WhenUser() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        PlaceOrderRequestDto request = new PlaceOrderRequestDto();
        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.CREATED);
        when(orderService.placeOrder(eq(userToken), any(PlaceOrderRequestDto.class))).thenAnswer(i -> response);

        mockMvc.perform(post("/api/order/place-order")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getShippingStatus_ShouldReturnResult_WhenUser() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        GetShippingOrderStatusRequest request = new GetShippingOrderStatusRequest();
        GetShippingOrderStatusResponseDto response = new GetShippingOrderStatusResponseDto();
        when(shippingService.getShippingOrderStatus(any(GetShippingOrderStatusRequest.class), eq(userToken)))
                .thenReturn(response);

        mockMvc.perform(post("/api/order/shipping-status")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenUser() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        OrderResponseDto orderResponse = new OrderResponseDto();
        when(orderService.getOrderById(1, "user-123")).thenReturn(orderResponse);

        mockMvc.perform(get("/api/order/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void createRefundOrder_ShouldReturnCreated_WhenMultipartRequest() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        CreateRefundOrderRequestDto requestDto = CreateRefundOrderRequestDto.builder()
                .orderId(1)
                .phoneNumber("0909090909")
                .note("Reason")
                .build();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        RefundOrder mockRefund = RefundOrder.builder()
                .id(100)
                .orderId(1)
                .userId("user-123")
                .status(RefundStatus.PENDING)
                .total(new BigDecimal("150000"))
                .createdAt(LocalDateTime.now())
                .build();

        when(refundOrderService.createRefundOrder(eq(userToken), any(CreateRefundOrderRequestDto.class), any()))
                .thenReturn(mockRefund);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("images", "evidence.jpg", "image/jpeg", "imageContent".getBytes());

        mockMvc.perform(multipart("/api/order/refund")
                        .file(requestPart)
                        .file(imagePart)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getUserRefundOrders_ShouldReturnPage_WhenUser() throws Exception {
        when(authService.validateToken("Bearer valid-token")).thenReturn(userToken);

        Page<UserRefundOrderDto> page = new PageImpl<>(List.of(
                UserRefundOrderDto.builder()
                        .id(100)
                        .orderId(1)
                        .total(new BigDecimal("150000"))
                        .status(RefundStatus.PENDING)
                        .build()
        ));
        when(refundOrderService.getUserRefundOrders(eq(userToken), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/order/refunds")
                        .header("Authorization", "Bearer valid-token")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100));
    }
}
