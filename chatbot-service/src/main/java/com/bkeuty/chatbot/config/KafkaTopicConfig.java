package com.bkeuty.chatbot.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic incomingChatTopic() {
        return new NewTopic("chat.incoming", 1, (short) 1);
    }

    @Bean
    public NewTopic persistChatTopic() {
        return new NewTopic("chat.persist", 1, (short) 1);
    }
}
