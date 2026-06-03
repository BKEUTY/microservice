package com.bkeuty.order.service.order;

import com.bkeuty.order.dto.order.OrderResponseDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.CartItemRepository;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.service.shipping.ShippingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private WebClient productWebClient;
    @Mock
    private WebClient promotionWebClient;
    @Mock
    private com.bkeuty.order.microservicecommunication.GHNCommunication ghnCommunication;
    @Mock
    private ShippingService shippingService;
    @Mock
    private KafkaTemplate<String, com.bkeuty.order.dto.order.DecreaseStockRequestDto> kafkaTemplate;
    @Mock
    private WebClient userWebClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrderById_ShouldReturnOrderResponseDto_WhenOrderExistsAndBelongsToUser() {
        Integer orderId = 1;
        String userId = "user123";

        Order mockOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .userName("Phong Nguyễn")
                .address("123 Street, District 1, HCM")
                .status(OrderStatus.NOT_CONFIRMED)
                .total(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("30000"))
                .orderDate(LocalDateTime.now())
                .orderItems(Collections.emptyList())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        OrderResponseDto response = orderService.getOrderById(orderId, userId);

        assertNotNull(response);
        assertEquals(orderId.toString(), response.getOrderId());
        assertEquals("Phong Nguyễn", response.getUserName());
        assertEquals(OrderStatus.NOT_CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("100000"), response.getTotal());
        
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_ShouldThrowResponseStatusException_WhenOrderNotFound() {
        Integer orderId = 999;
        String userId = "user123";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.getOrderById(orderId, userId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Order not found"));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_ShouldThrowResponseStatusException_WhenAccessDenied() {
        Integer orderId = 2;
        String requesterUserId = "user123";
        String actualOwnerId = "otherUser456";

        Order mockOrder = Order.builder()
                .id(orderId)
                .userId(actualOwnerId)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.getOrderById(orderId, requesterUserId);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Access denied to this order"));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getListOrders_ShouldReturnPageOfOrders_WhenValidInputsProvided() {
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);
        
        Order mockOrder = Order.builder()
                .id(1)
                .userId(userId)
                .userName("Phong Nguyễn")
                .status(OrderStatus.NOT_CONFIRMED)
                .total(new BigDecimal("100000"))
                .orderDate(LocalDateTime.now())
                .orderItems(Collections.emptyList())
                .build();
        
        Page<Order> pageOrders = new PageImpl<>(List.of(mockOrder));
        
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(pageOrders);

        Page<OrderResponseDto> response = orderService.getListOrders(userId, pageable, null, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Phong Nguyễn", response.getContent().get(0).getUserName());
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
}
