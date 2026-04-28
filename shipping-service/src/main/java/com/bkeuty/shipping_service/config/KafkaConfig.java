package com.bkeuty.shipping_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    NewTopic createShippingResponseTopic() {
        return new NewTopic("create-shipping-response-topic", 1, (short) 1);
    }

    @Bean
    NewTopic updateShippingStatusTopic() {
        return new NewTopic("update-shipping-status-topic", 1, (short) 1);
    }


}
