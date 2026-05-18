package com.bkeuty.order.service.shipping;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import com.bkeuty.order.repository.OrderRepository;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private GHNCommunication ghnCommunication;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ShippingService shippingService;

    @Test
    void getShippingOrderStatus_ShouldReturnDetail_WhenOrderExists() {
        GetShippingOrderStatusRequest request = new GetShippingOrderStatusRequest();
        request.setOrderId(1);
        
        TokenValidationResponseDto token = new TokenValidationResponseDto();
        token.setUserId("user123");

        Order mockOrder = Order.builder()
                .id(1)
                .userId("user123")
                .shippingCode("GHN12345")
                .build();

        GetShippingOrderStatusResponseDto mockData = new GetShippingOrderStatusResponseDto();
        mockData.setStatus("DELIVERED");

        GHNShippingDetailDto mockResponse = new GHNShippingDetailDto();
        mockResponse.setData(mockData);

        when(orderRepository.findByIdAndUserId(1, "user123")).thenReturn(mockOrder);
        when(ghnCommunication.getShippingStatus(any(OrderCodeDto.class))).thenReturn(Mono.just(mockResponse));

        GetShippingOrderStatusResponseDto result = shippingService.getShippingOrderStatus(request, token);

        assertNotNull(result);
        assertEquals("DELIVERED", result.getStatus());
        verify(orderRepository, times(1)).findByIdAndUserId(1, "user123");
        verify(ghnCommunication, times(1)).getShippingStatus(any(OrderCodeDto.class));
    }

    @Test
    void getShippingOrderStatus_ShouldThrowBadRequest_WhenOrderNotFound() {
        GetShippingOrderStatusRequest request = new GetShippingOrderStatusRequest();
        request.setOrderId(999);
        
        TokenValidationResponseDto token = new TokenValidationResponseDto();
        token.setUserId("user123");

        when(orderRepository.findByIdAndUserId(999, "user123")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shippingService.getShippingOrderStatus(request, token);
        });

        assertNotNull(exception);
        verify(ghnCommunication, never()).getShippingStatus(any());
    }
}
