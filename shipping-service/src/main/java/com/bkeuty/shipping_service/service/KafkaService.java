package com.bkeuty.shipping_service.service;

import com.bkeuty.shipping_service.dto.*;
import com.bkeuty.shipping_service.microservicecommunication.GHNCommunication;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaService {
    private final ShippingService shippingService;
    private final KafkaTemplate<String, CreateShippingResponseMessage>  kafkaTemplate;
    public KafkaService(ShippingService shippingService,  KafkaTemplate<String, CreateShippingResponseMessage> kafkaTemplate)
    {
        this.shippingService = shippingService;
        this.kafkaTemplate = kafkaTemplate;
    }
    @KafkaListener(topics = "create-shipping-order-topic")
    public void listenCreateShippingOrderTopic(CreateShippingOrderMessage message
    ){
        CreateShippingOrderResponseDto res = shippingService.createShippingOrder(message.getCreateShippingOrderDto()).block();
        if(res!=null){
            CreateShippingResponseMessage responseMessage = CreateShippingResponseMessage.builder()
                    .orderId(message.getOrderId()).shippingResponse(res).build();
            kafkaTemplate.send("create-shipping-response-topic", responseMessage);

        }
    }

}
