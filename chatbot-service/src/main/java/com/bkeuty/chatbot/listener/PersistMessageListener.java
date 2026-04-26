package com.bkeuty.chatbot.listener;

import com.bkeuty.chatbot.dto.ChatInteraction;
import com.bkeuty.chatbot.entity.ChatBucket;
import com.bkeuty.chatbot.repository.ChatBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class PersistMessageListener {
    private final ChatBucketRepository bucketRepository;
    private static final int MAX_MESSAGES_PER_BUCKET = 50;

    @KafkaListener(topics = "chat.persist", groupId = "chatbot-group")
    public void handlePersistMessage(ChatInteraction interaction) {
        String sessionId = interaction.getSessionId();
        
        int incomingMessageCount = interaction.getMessages() == null ? 0 : interaction.getMessages().size();
        ChatBucket bucket = bucketRepository.findTopBySessionIdOrderByBucketIndexDesc(sessionId)
                .filter(b -> (b.getMessageCount() == null ? 0 : b.getMessageCount()) + incomingMessageCount <= MAX_MESSAGES_PER_BUCKET)
                .orElseGet(() -> createNewBucket(sessionId));
        
        if (bucket.getMessages() == null) {
            bucket.setMessages(new ArrayList<>());
        }
        if (interaction.getMessages() != null) {
            bucket.getMessages().addAll(interaction.getMessages());
        }
        bucket.setMessageCount(bucket.getMessages().size());
        
        bucketRepository.save(bucket);
    }

    private ChatBucket createNewBucket(String sessionId) {
        Integer lastIndex = bucketRepository.findTopBySessionIdOrderByBucketIndexDesc(sessionId)
                .map(ChatBucket::getBucketIndex)
                .orElse(-1);

        return ChatBucket.builder()
                .sessionId(sessionId)
                .bucketIndex(lastIndex + 1)
                .messageCount(0)
                .messages(new ArrayList<>())
                .build();
    }
}
