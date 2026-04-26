package com.bkeuty.chatbot.service;

import com.bkeuty.chatbot.client.ProductClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private final ProductClient productClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private static final String GEMINI_MODEL = "gemini-3.1-flash-lite-preview";

    private String cachedCatalog = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL_MS = 300_000;

    private static final String SYSTEM_PROMPT = 
        "You are 'Bkeuty AI Assistant', a professional beauty expert for the Bkeuty platform.\n\n" +
        "DATA CONTEXT:\n" +
        "- Brands: CeraVe, La Roche-Posay, Estee Lauder, Dior, 3CE, Skin1004, Laneige, Kiehl's, Chanel, Shiseido, MAC, SK-II.\n" +
        "- Categories: Skincare (Sữa rửa mặt, Toner, Serum, Kem dưỡng, Chống nắng), Makeup (Son môi, Phấn nước/Cushion), Fragrance (Nước hoa nam/nữ).\n" +
        "- Key Options: 'Loại da' (Da nhạy cảm, Da khô, Da dầu, Mọi loại da), 'Dung tích', 'Màu sắc', 'Tone màu'.\n\n" +
        "MISSION:\n" +
        "1. Consult users based on their skin type and beauty needs.\n" +
        "2. Identify the exact 'productId' of the matching variant from the provided catalog.\n" +
        "3. PROACTIVE CLARIFICATION: If the user request is vague or matches multiple products, DO NOT guess. Instead, ask clarifying questions with specific options (e.g., 'Which brand do you prefer: CeraVe or La Roche-Posay?' or 'Are you looking for a Cleanser or a Serum?').\n" +
        "4. Always ask for 'Skin Type' if not provided for skincare requests.\n\n" +
        "OUTPUT FORMAT (STRICT JSON):\n" +
        "{\n" +
        "  \"text\": \"Your expert response in English here...\",\n" +
        "  \"recommendedProductId\": number or null\n" +
        "}\n\n" +
        "RULES:\n" +
        "- Use ONLY the products provided in the catalog context.\n" +
        "- If no suitable product is found, set 'recommendedProductId' to null and guide the user in the 'text' field.\n" +
        "- Response must be professional and sophisticated (English language).\n" +
        "- IMPORTANT: Output ONLY the JSON object. Do not use ```json markdown blocks.";

    public Map<String, Object> generateStructuredResponse(String chatHistory, String userPrompt, String language) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return Map.of("text", "Sorry, the AI system is currently not configured. Please try again later.", "recommendedProductId", null);
        }

        String targetLanguage = (language != null && language.equalsIgnoreCase("vi")) ? "Vietnamese" : "English";
        String dynamicSystemPrompt = SYSTEM_PROMPT.replace("Your expert response in English here...", "Your expert response in " + targetLanguage + " here...")
                .replace("Response must be professional and sophisticated (English language).", "Response must be professional and sophisticated in " + targetLanguage + ".");

        String productCatalog = getCachedProductCatalog();
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", 
                GEMINI_MODEL);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> systemInstruction = new HashMap<>();
            systemInstruction.put("parts", List.of(Map.of("text", dynamicSystemPrompt)));
            requestBody.put("system_instruction", systemInstruction);

            String fullPrompt = String.format(
                "PRODUCT CATALOG:\n%s\n\nCHAT HISTORY:\n%s\n\nUSER MESSAGE: %s", 
                productCatalog, chatHistory, userPrompt);
            
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(Map.of("text", fullPrompt)));
            requestBody.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            String responseJson = restTemplate.postForObject(url, requestEntity, String.class);
            if (responseJson == null || responseJson.isBlank()) {
                return Map.of("text", "I'm having trouble processing your request. Could you please rephrase that?", "recommendedProductId", null);
            }

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isMissingNode() || candidates.size() == 0) {
                return Map.of("text", "I'm having trouble processing your request. Could you please rephrase that?", "recommendedProductId", null);
            }

            JsonNode textNode = candidates.path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.isNull()) {
                return Map.of("text", "I'm having trouble processing your request. Could you please rephrase that?", "recommendedProductId", null);
            }
            
            String aiJson = textNode.asText();
            aiJson = aiJson.replaceAll("(?s)^.*?\\{", "{").replaceAll("\\}.*?$", "}");
            
            return objectMapper.readValue(aiJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            String exceptionMessage = String.valueOf(e.getMessage());
            log.error("Gemini API Error for user prompt '{}': ", userPrompt, e);
            String errorMsg = "We're sorry, an error occurred while connecting to our beauty expert AI.";
            if (exceptionMessage.contains("429")) {
                errorMsg = "The AI system is currently overloaded (Rate limit). Please try again in a few moments.";
            } else if (exceptionMessage.contains("403") || exceptionMessage.contains("401")) {
                errorMsg = "AI API Key authentication failure. Please check the system configuration.";
            }
            return Map.of("text", errorMsg, "recommendedProductId", null);
        }
    }

    private synchronized String getCachedProductCatalog() {
        long currentTime = System.currentTimeMillis();
        if (cachedCatalog == null || (currentTime - lastCacheUpdate) > CACHE_TTL_MS) {
            try {
                cachedCatalog = productClient.getProductContext();
                lastCacheUpdate = currentTime;
                log.info("Product catalog cache updated.");
            } catch (Exception e) {
                log.error("Failed to update product catalog cache: {}", e.getMessage());
                return cachedCatalog != null ? cachedCatalog : "[]";
            }
        }
        return cachedCatalog;
    }
}
