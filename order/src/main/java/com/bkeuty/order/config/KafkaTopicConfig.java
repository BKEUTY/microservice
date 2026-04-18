package com.bkeuty.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic orderTopic(){
        return new NewTopic("place-order-topic", 1, (short) 1);
    }
    @Bean
    public NewTopic decreaseStockTopic(){
        return new NewTopic("decrease-stock-topic", 1, (short) 1);
    }
    @Bean
    public NewTopic createShippingOrderTopic(){
        return new NewTopic("create-shipping-order-topic", 1, (short) 1);
    }

}
