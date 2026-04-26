package com.bkeuty.chatbot.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic persistChatTopic() {
        return new NewTopic("chat.persist", 1, (short) 1);
    }
}
