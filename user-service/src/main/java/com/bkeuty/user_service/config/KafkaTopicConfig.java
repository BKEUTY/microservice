package com.bkeuty.user_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic refundSuccessTopic(){
        return new NewTopic("refund-wallet-success-topic", 1, (short) 1);
    }
}
