package com.bkeuty.chatbot.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    public static final String CHAT_PERSIST_TOPIC = "chat.persist";

    @Bean
    public NewTopic persistChatTopic() {
        return new NewTopic(CHAT_PERSIST_TOPIC, 1, (short) 1);
    }
}
