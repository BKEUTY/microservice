package com.bkeuty.shipping_service.service;

import com.bkeuty.shipping_service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    @Mock
    private ShippingService shippingService;

    @Mock
    private KafkaTemplate<String, CreateShippingResponseMessage> kafkaTemplate;

    @Mock
    private KafkaTemplate<String, GhnWebhookDto> updateShippingStatusKafkaTemplate;

    private KafkaService kafkaService;

    @BeforeEach
    void setUp() {
        kafkaService = new KafkaService(shippingService, kafkaTemplate, updateShippingStatusKafkaTemplate);
    }

    @Test
    void listenCreateShippingOrderTopic_ShouldCallShippingServiceAndPublishResponse_WhenSuccessful() {
        CreateShippingOrderDto orderDto = new CreateShippingOrderDto();
        CreateShippingOrderMessage message = CreateShippingOrderMessage.builder()
                .orderId(123)
                .createShippingOrderDto(orderDto)
                .build();

        CreateShippingOrderResponseDto responseDto = new CreateShippingOrderResponseDto();
        responseDto.setCode(200);
        responseDto.setMessage("Success");
        
        ShippingOrderDto shippingOrderDto = new ShippingOrderDto();
        shippingOrderDto.setOrderCode("GHN-ABC-123");
        responseDto.setData(shippingOrderDto);

        when(shippingService.createShippingOrder(orderDto)).thenReturn(Mono.just(responseDto));

        kafkaService.listenCreateShippingOrderTopic(message);

        ArgumentCaptor<CreateShippingResponseMessage> captor = ArgumentCaptor.forClass(CreateShippingResponseMessage.class);
        verify(kafkaTemplate, times(1)).send(eq("create-shipping-response-topic"), captor.capture());

        CreateShippingResponseMessage sent = captor.getValue();
        assertEquals(123, sent.getOrderId());
        assertEquals(responseDto, sent.getShippingResponse());
    }

    @Test
    void listenCreateShippingOrderTopic_ShouldDoNothing_WhenShippingServiceReturnsNull() {
        CreateShippingOrderDto orderDto = new CreateShippingOrderDto();
        CreateShippingOrderMessage message = CreateShippingOrderMessage.builder()
                .orderId(123)
                .createShippingOrderDto(orderDto)
                .build();

        when(shippingService.createShippingOrder(orderDto)).thenReturn(Mono.empty());

        kafkaService.listenCreateShippingOrderTopic(message);

        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendUpdateShippingOrder_ShouldPublishToKafka() {
        GhnWebhookDto dto = new GhnWebhookDto();
        dto.setOrderCode("GHN-ABC-123");
        dto.setStatus("ready_to_pick");

        kafkaService.sendUpdateShippingOrder(dto);

        verify(updateShippingStatusKafkaTemplate, times(1))
                .send(eq("update-shipping-status-topic"), eq(dto));
    }
}
