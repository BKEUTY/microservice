package com.bkeuty.chatbot.service;

import com.bkeuty.chatbot.client.ProductClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("externalRestTemplate")
    private final RestTemplate externalRestTemplate;
    private static final String GEMINI_MODEL = "gemini-3.1-flash-lite-preview";

    private volatile String cachedCatalog = null;
    private volatile long lastCacheUpdate = 0;
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
        "OUTPUT FORMAT (STRICT JSON ONLY):\n" +
        "Return a JSON object with this structure:\n" +
        "{\"text\": \"your_consultation_response\", \"recommendedProductId\": productId_or_null}\n\n" +
        "RULES:\n" +
        "- Use ONLY the products provided in the catalog context.\n" +
        "- If no suitable product is found, set 'recommendedProductId' to null and guide the user in the 'text' field.\n" +
        "- DO NOT offer ordering, payment, or shipping services. The chatbot only provides recommendations.\n" +
        "- NEVER say phrases like 'Bạn có muốn đặt hàng không?' or 'Tôi sẽ hỗ trợ bạn đặt hàng'.\n" +
        "- If the user asks about ordering, tell them to click on the product card to view details and buy on the website.\n" +
        "- CRITICAL: The 'recommendedProductId' MUST match the specific product mentioned in your response text.\n" +
        "- IMPORTANT: Output ONLY the JSON object. Do not use markdown blocks.";

    public Map<String, Object> generateStructuredResponse(String chatHistory, String userPrompt, String language) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return createResponseMap("Sorry, the AI system is currently not configured. Please try again later.", null);
        }

        String targetLanguage = (language != null && language.equalsIgnoreCase("vi")) ? "Vietnamese" : "English";
        String dynamicSystemPrompt = SYSTEM_PROMPT + "\n- Response must be professional and sophisticated in " + targetLanguage + ".";

        String productCatalog = getCachedProductCatalog();
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", 
                GEMINI_MODEL);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> systemInstruction = new HashMap<>();
            systemInstruction.put("parts", List.of(Map.of("text", dynamicSystemPrompt)));
            requestBody.put("system_instruction", systemInstruction);

            String prunedHistory = chatHistory;
            if (chatHistory != null && chatHistory.length() > 2000) {
                prunedHistory = "..." + chatHistory.substring(chatHistory.length() - 2000);
            }

            String fullPrompt = String.format(
                "PRODUCT CATALOG (Condensed):\n%s\n\nCHAT HISTORY (Recent):\n%s\n\nUSER MESSAGE: %s", 
                productCatalog, prunedHistory, userPrompt);
            
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(Map.of("text", fullPrompt)));
            requestBody.put("contents", List.of(content));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            String responseJson = externalRestTemplate.postForObject(url, requestEntity, String.class);
            if (responseJson == null || responseJson.isBlank()) {
                return createResponseMap("I'm having trouble processing your request. Could you please rephrase that?", null);
            }

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isMissingNode() || candidates.size() == 0) {
                return createResponseMap("I'm having trouble processing your request. Could you please rephrase that?", null);
            }

            JsonNode textNode = candidates.path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.isNull()) {
                return createResponseMap("I'm having trouble processing your request. Could you please rephrase that?", null);
            }
            
            String aiOutput = textNode.asText();
            int jsonStart = aiOutput.indexOf('{');
            int jsonEnd = aiOutput.lastIndexOf('}');
            
            if (jsonStart < 0 || jsonEnd < jsonStart) {
                log.error("Invalid AI response format: No JSON object found in output");
                return createResponseMap("I'm having trouble formatting my response. Could you please try again?", null);
            }
            
            String extractedJson = aiOutput.substring(jsonStart, jsonEnd + 1);
            return objectMapper.readValue(extractedJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            String exceptionMessage = String.valueOf(e.getMessage());
            log.error("Gemini API Error while generating structured response: ", e);
            String errorMsg = "We're sorry, an error occurred while connecting to our beauty expert AI.";
            if (exceptionMessage.contains("429")) {
                errorMsg = "The AI system is currently overloaded (Rate limit). Please try again in a few moments.";
            } else if (exceptionMessage.contains("503")) {
                errorMsg = "Gemini AI is currently experiencing high demand. Please try again later.";
            } else if (exceptionMessage.contains("403") || exceptionMessage.contains("401")) {
                errorMsg = "AI API Key authentication failure. Please check the system configuration.";
            }
            return createResponseMap(errorMsg, null);
        }
    }

    private Map<String, Object> createResponseMap(String text, Object productId) {
        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("recommendedProductId", productId);
        return response;
    }

    private String getCachedProductCatalog() {
        long currentTime = System.currentTimeMillis();
        if (cachedCatalog == null || (currentTime - lastCacheUpdate) > CACHE_TTL_MS) {
            synchronized (this) {
                if (cachedCatalog == null || (System.currentTimeMillis() - lastCacheUpdate) > CACHE_TTL_MS) {
                    try {
                        String freshCatalog = productClient.getProductContext();
                        if (freshCatalog == null || freshCatalog.isBlank() || freshCatalog.equals("[]")) {
                            cachedCatalog = "[]";
                        } else {
                            JsonNode rootNode = objectMapper.readTree(freshCatalog);
                            JsonNode contentNode = rootNode.path("content");
                            JsonNode products = contentNode.isArray() ? contentNode : rootNode;
                            
                            List<Map<String, Object>> prunedList = new java.util.ArrayList<>();
                            if (products.isArray()) {
                                for (JsonNode p : products) {
                                    Map<String, Object> pruned = new HashMap<>();
                                    pruned.put("id", p.path("productId").asLong());
                                    pruned.put("name", p.path("variantName").asText());
                                    pruned.put("price", p.path("discountPrice").asDouble());
                                    prunedList.add(pruned);
                                }
                            }
                            cachedCatalog = objectMapper.writeValueAsString(prunedList);
                        }
                        lastCacheUpdate = System.currentTimeMillis();
                        log.info("Optimized product catalog cache updated.");
                    } catch (Exception e) {
                        log.error("Failed to update product catalog cache: {}", e.getMessage());
                        lastCacheUpdate = System.currentTimeMillis();
                        if (cachedCatalog == null) {
                            cachedCatalog = "[]";
                        }
                    }
                }
            }
        }
        return cachedCatalog != null ? cachedCatalog : "[]";
    }
}
