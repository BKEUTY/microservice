package com.bkeuty.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic paymentTransactionTopic() {
        return new NewTopic("payment-transaction-topic", 1, (short) 1);
    }
}
