package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.service.membership.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WebClient productWebClient;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private AdminOrderService adminOrderService;

    private Order mockOrder;

    @BeforeEach
    void setUp() {
        OrderItem item = new OrderItem();
        item.setProductVariantId(10);
        item.setProductVariantName("Serum Dưỡng Da");
        item.setQuantity(2);
        item.setProductVariantPrice(new BigDecimal("500000"));

        mockOrder = Order.builder()
                .id(1)
                .userId("user123")
                .status(OrderStatus.NOT_CONFIRMED) // Current status
                .total(new BigDecimal("1000000"))
                .orderItems(List.of(item))
                .build();
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatusAndReturnDto_WhenValidInputsProvided() {
        String newStatusStr = "CONFIRMED";
        String token = "Bearer token123";

        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminOrderDto result = adminOrderService.updateOrderStatus(1, newStatusStr, token);

        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(1, result.getOrderId());
        verify(orderRepository, times(1)).saveAndFlush(mockOrder);
    }

    @Test
    void updateOrderStatus_ShouldTriggerMembershipRecalculation_WhenStatusIsSucceeded() {
        String newStatusStr = "SUCCEEDED";
        String token = "Bearer token123";

        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminOrderDto result = adminOrderService.updateOrderStatus(1, newStatusStr, token);

        assertEquals("SUCCEEDED", result.getStatus());
        verify(membershipService, times(1)).recalculateMembershipLevel("user123");
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adminOrderService.updateOrderStatus(999, "CONFIRMED", "token");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenStatusIsInvalid() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adminOrderService.updateOrderStatus(1, "INVALID_STATUS_XXX", "token");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid order status"));
    }
}
