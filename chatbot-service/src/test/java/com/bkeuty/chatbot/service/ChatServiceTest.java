package com.bkeuty.chatbot.service;

import com.bkeuty.chatbot.client.ProductClient;
import com.bkeuty.chatbot.dto.ChatInteraction;
import com.bkeuty.chatbot.dto.ChatRequest;
import com.bkeuty.chatbot.dto.ChatResponse;
import com.bkeuty.chatbot.dto.ProductDetailDto;
import com.bkeuty.chatbot.entity.ChatBucket;
import com.bkeuty.chatbot.entity.ChatMessage;
import com.bkeuty.chatbot.repository.ChatBucketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private GeminiService geminiService;
    @Mock private ChatContextService contextService;
    @Mock private ProductClient productClient;
    @Mock private ChatBucketRepository bucketRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ChatService chatService;

    private static final String SESSION_ID = "session-123";
    private static final String USER_ID = "user-uuid";

    @Test
    void getChatHistory_ShouldCappedAt50Messages_WhenHistoryIsLong() {
        List<ChatMessage> list = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            list.add(ChatMessage.builder().sender("user").content("Msg " + i).build());
        }

        ChatBucket bucket = new ChatBucket();
        bucket.setMessages(list);

        when(bucketRepository.findAllBySessionIdOrderByBucketIndexAsc(SESSION_ID))
                .thenReturn(List.of(bucket));

        List<ChatMessage> history = chatService.getChatHistory(SESSION_ID);

        assertNotNull(history);
        assertEquals(50, history.size());
        assertEquals("Msg 10", history.get(0).getContent());
        assertEquals("Msg 59", history.get(49).getContent());
    }

    @Test
    void getChatHistory_ShouldReturnAll_WhenHistoryIsShort() {
        ChatMessage m = ChatMessage.builder().sender("user").content("Hello").build();
        ChatBucket bucket = new ChatBucket();
        bucket.setMessages(List.of(m));

        when(bucketRepository.findAllBySessionIdOrderByBucketIndexAsc(SESSION_ID))
                .thenReturn(List.of(bucket));

        List<ChatMessage> history = chatService.getChatHistory(SESSION_ID);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("Hello", history.get(0).getContent());
    }

    @Test
    void processChatMessage_ShouldReturnSuccess_WithProductRecommendation() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Da dầu cần gì?");
        request.setLanguage("vi");
        request.setUserId(USER_ID);
        request.setMembershipLevel(2);

        Map<String, Object> geminiResult = Map.of(
                "text", "Bạn nên dùng kem dưỡng A.",
                "recommendedProductId", 101
        );

        ProductDetailDto productDetail = new ProductDetailDto();
        productDetail.setProductId(101);
        productDetail.setVariantName("Kem dưỡng ẩm A");

        when(contextService.getFormattedContext(SESSION_ID)).thenReturn("User: Cần tư vấn");
        when(geminiService.generateStructuredResponse(any(), any(), any(), any(), any()))
                .thenReturn(geminiResult);
        when(productClient.getProductById(101, USER_ID, 2)).thenReturn(productDetail);

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Bạn nên dùng kem dưỡng A.", response.getResponse());
        assertEquals(1, response.getRecommendedProduct().size());
        assertEquals("Kem dưỡng ẩm A", response.getRecommendedProduct().get(0).getVariantName());

        verify(contextService, times(2)).appendMessage(eq(SESSION_ID), any());
        verify(kafkaTemplate, times(1)).send(any(), eq(SESSION_ID), any(ChatInteraction.class));
    }

    @Test
    void processChatMessage_ShouldHandleRecommendedProductIdAsString() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Tư vấn đi");
        request.setLanguage("vi");
        request.setUserId(USER_ID);
        request.setMembershipLevel(0);

        Map<String, Object> geminiResult = Map.of(
                "text", "Sản phẩm 202 phù hợp.",
                "recommendedProductId", "202"
        );

        ProductDetailDto productDetail = new ProductDetailDto();
        productDetail.setProductId(202);

        when(contextService.getFormattedContext(SESSION_ID)).thenReturn("");
        when(geminiService.generateStructuredResponse(any(), any(), any(), any(), any()))
                .thenReturn(geminiResult);
        when(productClient.getProductById(202, USER_ID, 0)).thenReturn(productDetail);

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals(1, response.getRecommendedProduct().size());
        assertEquals(202, response.getRecommendedProduct().get(0).getProductId());
    }

    @Test
    void processChatMessage_ShouldReturnError_WhenExceptionThrown() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Lỗi hệ thống");

        when(contextService.getFormattedContext(SESSION_ID)).thenThrow(new RuntimeException("Redis down"));

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals("error", response.getStatus());
        assertTrue(response.getResponse().contains("AI expert"));
    }

    @Test
    void processChatMessage_ShouldContinue_WhenProductIdStringIsInvalid() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Test invalid ID string");

        Map<String, Object> geminiResult = Map.of(
                "text", "Sản phẩm không hợp lệ.",
                "recommendedProductId", "invalid_id_123"
        );

        when(contextService.getFormattedContext(SESSION_ID)).thenReturn("");
        when(geminiService.generateStructuredResponse(any(), any(), any(), any(), any()))
                .thenReturn(geminiResult);

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertTrue(response.getRecommendedProduct().isEmpty());
    }

    @Test
    void processChatMessage_ShouldContinue_WhenProductClientThrowsException() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Test product service error");

        Map<String, Object> geminiResult = Map.of(
                "text", "Sản phẩm 303.",
                "recommendedProductId", 303
        );

        when(contextService.getFormattedContext(SESSION_ID)).thenReturn("");
        when(geminiService.generateStructuredResponse(any(), any(), any(), any(), any()))
                .thenReturn(geminiResult);
        when(productClient.getProductById(303, null, 0)).thenThrow(new RuntimeException("Product service timeout"));

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertTrue(response.getRecommendedProduct().isEmpty());
    }

    @Test
    void processChatMessage_ShouldContinue_WhenRedisThrowsException() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Test Redis error");

        Map<String, Object> geminiResult = Map.of(
                "text", "Sản phẩm 404.",
                "recommendedProductId", 404
        );

        ProductDetailDto productDetail = new ProductDetailDto();
        productDetail.setProductId(404);

        when(contextService.getFormattedContext(SESSION_ID)).thenReturn("");
        when(geminiService.generateStructuredResponse(any(), any(), any(), any(), any()))
                .thenReturn(geminiResult);
        when(productClient.getProductById(eq(404), any(), any())).thenReturn(productDetail);
        doThrow(new RuntimeException("Redis connection lost")).when(contextService).appendMessage(eq(SESSION_ID), any());

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(1, response.getRecommendedProduct().size());
    }

    @Test
    void processChatMessage_ShouldReturnPersistenceFailed_WhenKafkaThrowsException() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(SESSION_ID);
        request.setMessage("Test Kafka error");

        Map<String, Object> geminiResult = Map.of(
                "text", "Chào bạn"
        );

        when(contextService.getFormattedContext(SESSION_ID)).thenReturn("");
        when(geminiService.generateStructuredResponse(any(), any(), any(), any(), any()))
                .thenReturn(geminiResult);
        when(kafkaTemplate.send(any(), any(), any())).thenThrow(new RuntimeException("Kafka producer buffer full"));

        ChatResponse response = chatService.processChatMessage(request);

        assertNotNull(response);
        assertEquals("persistence_failed", response.getStatus());
        assertEquals("Chào bạn", response.getResponse());
    }

    @Test
    void checkHealth_ShouldReportOK_WhenAllServicesAreUp() {
        when(bucketRepository.count()).thenReturn(5L);
        when(contextService.getFormattedContext("health-check")).thenReturn("");
        when(productClient.getProductContext("health-check", 0)).thenReturn("[]");

        String status = chatService.checkHealth();

        assertEquals("OK", status);
    }

    @Test
    void checkHealth_ShouldReportDownServices_WhenConnectionsFail() {
        when(bucketRepository.count()).thenThrow(new RuntimeException("Mongo connection refused"));
        when(contextService.getFormattedContext("health-check")).thenThrow(new RuntimeException("Redis timeout"));
        when(productClient.getProductContext("health-check", 0)).thenThrow(new RuntimeException("Product service 503"));
        when(kafkaTemplate.partitionsFor(any())).thenThrow(new RuntimeException("Kafka disconnected"));

        String status = chatService.checkHealth();

        assertTrue(status.contains("MONGODB_DOWN"));
        assertTrue(status.contains("REDIS_DOWN"));
        assertTrue(status.contains("PRODUCT_SERVICE_DOWN"));
        assertTrue(status.contains("KAFKA_DOWN"));
    }
}
