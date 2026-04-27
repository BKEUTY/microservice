package com.bkeuty.chatbot.service;

import com.bkeuty.chatbot.client.ProductClient;
import com.bkeuty.chatbot.dto.ChatInteraction;
import com.bkeuty.chatbot.dto.ChatRequest;
import com.bkeuty.chatbot.dto.ChatResponse;
import com.bkeuty.chatbot.dto.ProductDetailDto;
import com.bkeuty.chatbot.entity.ChatMessage;
import com.bkeuty.chatbot.repository.ChatBucketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bkeuty.chatbot.config.KafkaTopicConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final GeminiService geminiService;
    private final ChatContextService contextService;
    private final ProductClient productClient;
    private final ChatBucketRepository bucketRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public List<ChatMessage> getChatHistory(String sessionId) {
        return bucketRepository.findAllBySessionIdOrderByBucketIndexAsc(sessionId)
                .stream()
                .flatMap(bucket -> java.util.Optional.ofNullable(bucket.getMessages())
                        .orElseGet(java.util.Collections::emptyList)
                        .stream())
                .collect(java.util.stream.Collectors.toList());
    }

    public ChatResponse processChatMessage(ChatRequest request) {
        String sessionId = request.getSessionId();
        String userMessageContent = request.getMessage();

        try {
            ChatMessage userMessage = ChatMessage.builder()
                    .sender("user")
                    .content(userMessageContent)
                    .timestamp(LocalDateTime.now())
                    .build();

            String context = contextService.getFormattedContext(sessionId);

            Map<String, Object> aiResult = geminiService.generateStructuredResponse(context, userMessageContent, request.getLanguage());
            
            String aiResponseContent = (String) aiResult.getOrDefault("text", "I'm sorry, I couldn't process that request.");
            Object productIdObj = aiResult.get("recommendedProductId");
            Integer recommendedProductId = null;
            
            if (productIdObj instanceof Number) {
                recommendedProductId = ((Number) productIdObj).intValue();
            } else if (productIdObj instanceof String) {
                try {
                    recommendedProductId = Integer.parseInt((String) productIdObj);
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse recommendedProductId from string: {}", productIdObj);
                }
            }
            
            ProductDetailDto productDetails = null;
            if (recommendedProductId != null) {
                try {
                    productDetails = productClient.getProductById(recommendedProductId);
                } catch (Exception e) {
                    log.error("Error fetching product details: {}", e.getMessage());
                }
            }

            ChatMessage aiMessage = ChatMessage.builder()
                    .sender("ai")
                    .content(aiResponseContent)
                    .timestamp(LocalDateTime.now())
                    .recommendedProduct(productDetails != null ? List.of(productDetails) : List.of())
                    .build();

            try {
                contextService.appendMessage(sessionId, userMessage);
                contextService.appendMessage(sessionId, aiMessage);
            } catch (Exception e) {
                log.error("Error saving messages to Redis: {}", e.getMessage());
            }

            ChatInteraction interaction = ChatInteraction.builder()
                    .sessionId(sessionId)
                    .messages(List.of(userMessage, aiMessage))
                    .build();
            
            String status = "success";
            try {
                kafkaTemplate.send(KafkaTopicConfig.CHAT_PERSIST_TOPIC, sessionId, interaction);
            } catch (Exception e) {
                log.error("Error sending message to Kafka for session {}: {}", sessionId, e.getMessage());
                status = "persistence_failed";
            }

            return ChatResponse.builder()
                    .sessionId(sessionId)
                    .response(aiResponseContent)
                    .recommendedProduct(productDetails != null ? List.of(productDetails) : List.of())
                    .status(status)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Critical error in processChatMessage: ", e);
            return ChatResponse.builder()
                    .sessionId(sessionId)
                    .response("I'm having trouble connecting to the AI expert right now. Please try again.")
                    .status("error")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
}
