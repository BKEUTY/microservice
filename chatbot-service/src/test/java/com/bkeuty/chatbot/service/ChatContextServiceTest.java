package com.bkeuty.chatbot.service;

import com.bkeuty.chatbot.entity.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatContextServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatContextService chatContextService;

    private static final String SESSION_ID = "session-abc-123";
    private static final String REDIS_KEY = "chat:session:" + SESSION_ID;

    @Test
    void appendMessage_ShouldPushToRedisAndSetTTL() {
        ChatMessage message = ChatMessage.builder()
                .sender("user")
                .content("Hello!")
                .build();

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.rightPush(anyString(), any())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        assertDoesNotThrow(() -> chatContextService.appendMessage(SESSION_ID, message));

        verify(listOperations, times(1)).rightPush(eq(REDIS_KEY), any(ChatMessage.class));
        verify(listOperations, times(1)).trim(eq(REDIS_KEY), eq(-10L), eq(-1L));
        verify(redisTemplate, times(1)).expire(eq(REDIS_KEY), eq(2L), eq(TimeUnit.HOURS));
    }

    @Test
    void appendMessage_ShouldDoNothing_WhenMessageIsNull() {
        chatContextService.appendMessage(SESSION_ID, null);
        verify(redisTemplate, never()).opsForList();
    }

    @Test
    void getRecentMessages_ShouldReturnMappedMessages_WhenRedisHasData() {
        ChatMessage msg1 = ChatMessage.builder().sender("user").content("Hi").build();
        ChatMessage msg2 = ChatMessage.builder().sender("ai").content("Hello!").build();

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(REDIS_KEY, 0, -1)).thenReturn(List.of(msg1, msg2));
        when(objectMapper.convertValue(msg1, ChatMessage.class)).thenReturn(msg1);
        when(objectMapper.convertValue(msg2, ChatMessage.class)).thenReturn(msg2);

        List<ChatMessage> result = chatContextService.getRecentMessages(SESSION_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user", result.get(0).getSender());
        assertEquals("ai", result.get(1).getSender());
    }

    @Test
    void getRecentMessages_ShouldReturnEmpty_WhenRedisReturnsNull() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(REDIS_KEY, 0, -1)).thenReturn(null);

        List<ChatMessage> result = chatContextService.getRecentMessages(SESSION_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getFormattedContext_ShouldReturnFormattedString() {
        ChatMessage msg1 = ChatMessage.builder().sender("user").content("Sản phẩm này tốt không?").build();
        ChatMessage msg2 = ChatMessage.builder().sender("ai").content("Rất tốt!").build();

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(REDIS_KEY, 0, -1)).thenReturn(List.of(msg1, msg2));
        when(objectMapper.convertValue(msg1, ChatMessage.class)).thenReturn(msg1);
        when(objectMapper.convertValue(msg2, ChatMessage.class)).thenReturn(msg2);

        String context = chatContextService.getFormattedContext(SESSION_ID);

        assertNotNull(context);
        assertTrue(context.contains("user: Sản phẩm này tốt không?"));
        assertTrue(context.contains("ai: Rất tốt!"));
    }

    @Test
    void getFormattedContext_ShouldReturnEmptyString_WhenNoMessages() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(REDIS_KEY, 0, -1)).thenReturn(null);

        String context = chatContextService.getFormattedContext(SESSION_ID);

        assertEquals("", context);
    }
}
