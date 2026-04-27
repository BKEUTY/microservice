package com.bkeuty.chatbot.listener;

import com.bkeuty.chatbot.config.KafkaTopicConfig;
import com.bkeuty.chatbot.dto.ChatInteraction;
import com.bkeuty.chatbot.entity.ChatBucket;
import com.bkeuty.chatbot.repository.ChatBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PersistMessageListener {
    private final ChatBucketRepository bucketRepository;
    private static final int MAX_MESSAGES_PER_BUCKET = 50;

    private final ConcurrentHashMap<String, Object> sessionLocks = new ConcurrentHashMap<>();

    @KafkaListener(topics = KafkaTopicConfig.CHAT_PERSIST_TOPIC, groupId = "chatbot-group")
    public void handlePersistMessage(ChatInteraction interaction) {
        String sessionId = interaction.getSessionId();
        Object sessionLock = sessionLocks.computeIfAbsent(sessionId, key -> new Object());

        synchronized (sessionLock) {
            try {
                int incomingMessageCount = interaction.getMessages() == null ? 0 : interaction.getMessages().size();             
                ChatBucket lastBucket = bucketRepository.findTopBySessionIdOrderByBucketIndexDesc(sessionId).orElse(null);
                ChatBucket bucket;

                if (lastBucket != null && (lastBucket.getMessageCount() == null ? 0 : lastBucket.getMessageCount()) + incomingMessageCount <= MAX_MESSAGES_PER_BUCKET) {
                    bucket = lastBucket;
                } else {
                    int nextIndex = (lastBucket == null) ? 0 : lastBucket.getBucketIndex() + 1;
                    bucket = createNewBucket(sessionId, nextIndex);
                }

                if (bucket.getMessages() == null) {
                    bucket.setMessages(new ArrayList<>());
                }
                if (interaction.getMessages() != null) {
                    bucket.getMessages().addAll(interaction.getMessages());
                }
                bucket.setMessageCount(bucket.getMessages().size());

                bucketRepository.save(bucket);
            } finally {
                sessionLocks.remove(sessionId, sessionLock);
            }
        }
    }

    private ChatBucket createNewBucket(String sessionId, int bucketIndex) {
        return ChatBucket.builder()
                .sessionId(sessionId)
                .bucketIndex(bucketIndex)
                .messageCount(0)
                .messages(new ArrayList<>())
                .build();
    }
}
