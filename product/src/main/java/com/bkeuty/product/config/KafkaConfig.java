package com.bkeuty.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic decreaseStockStatusTopic(){
        return new NewTopic("decrease-stock-status-topic", 1, (short) 1);
    }
}
