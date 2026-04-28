package com.bkeuty.chatbot.service;

import com.bkeuty.chatbot.entity.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatContextService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String REDIS_KEY_PREFIX = "chat:session:";
    private static final int MAX_CONTEXT_MESSAGES = 10;
    private static final int TTL_HOURS = 2;

    public void appendMessage(String sessionId, ChatMessage message) {
        if (message == null) return;
        
        String key = REDIS_KEY_PREFIX + sessionId;
        
        ChatMessage contextMessage = ChatMessage.builder()
                .sender(message.getSender())
                .content(message.getContent())
                .build();
                
        redisTemplate.opsForList().rightPush(key, contextMessage);
        redisTemplate.opsForList().trim(key, -MAX_CONTEXT_MESSAGES, -1);
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
    }

    public List<ChatMessage> getRecentMessages(String sessionId) {
        String key = REDIS_KEY_PREFIX + sessionId;
        try {
            List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
            if (messages == null) return Collections.emptyList();
            return messages.stream()
                .map(obj -> {
                    try {
                        return objectMapper.convertValue(obj, ChatMessage.class);
                    } catch (Exception e) {
                        log.error("Failed to convert Redis object to ChatMessage: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving messages from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getFormattedContext(String sessionId) {
        List<ChatMessage> messages = getRecentMessages(sessionId);
        return messages.stream()
                .map(msg -> String.format("%s: %s", msg.getSender(), msg.getContent()))
                .collect(Collectors.joining("\n"));
    }
}
