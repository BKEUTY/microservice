package com.bkeuty.shipping_service.service;

import com.bkeuty.shipping_service.dto.*;
import com.bkeuty.shipping_service.microservicecommunication.GHNCommunication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private GHNCommunication ghnCommunication;

    @InjectMocks
    private ShippingService shippingService;

    @Test
    void calShippingFee_ShouldDelegate_ToGHNCommunication() {
        CalShippingFeeDto request = new CalShippingFeeDto();
        CalShippingFeeResponseDto response = new CalShippingFeeResponseDto();

        when(ghnCommunication.getCalShippingFee(request)).thenReturn(Mono.just(response));

        Mono<CalShippingFeeResponseDto> result = shippingService.calShippingFee(request);

        assertNotNull(result);
        assertEquals(response, result.block());
        verify(ghnCommunication, times(1)).getCalShippingFee(request);
    }

    @Test
    void calShippingTime_ShouldDelegate_ToGHNCommunication() {
        CalShippingTimeDto request = new CalShippingTimeDto();
        CalShippingTimeResponseDto response = new CalShippingTimeResponseDto();

        when(ghnCommunication.getCalShippingTime(request)).thenReturn(Mono.just(response));

        Mono<CalShippingTimeResponseDto> result = shippingService.calShippingTime(request);

        assertNotNull(result);
        assertEquals(response, result.block());
        verify(ghnCommunication, times(1)).getCalShippingTime(request);
    }

    @Test
    void createShippingOrder_ShouldDelegate_ToGHNCommunication() {
        CreateShippingOrderDto request = new CreateShippingOrderDto();
        CreateShippingOrderResponseDto response = new CreateShippingOrderResponseDto();

        when(ghnCommunication.createShippingOrder(request)).thenReturn(Mono.just(response));

        Mono<CreateShippingOrderResponseDto> result = shippingService.createShippingOrder(request);

        assertNotNull(result);
        assertEquals(response, result.block());
        verify(ghnCommunication, times(1)).createShippingOrder(request);
    }

    @Test
    void createRefundShippingOrder_ShouldDelegate_ToGHNCommunication() {
        CreateRefundShippingDto request = new CreateRefundShippingDto();
        CreateShippingOrderResponseDto response = new CreateShippingOrderResponseDto();

        when(ghnCommunication.createRefundOrder(request)).thenReturn(Mono.just(response));

        Mono<CreateShippingOrderResponseDto> result = shippingService.createRefundShippingOrder(request);

        assertNotNull(result);
        assertEquals(response, result.block());
        verify(ghnCommunication, times(1)).createRefundOrder(request);
    }
}
