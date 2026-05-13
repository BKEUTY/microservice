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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.core.type.TypeReference;

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

    private final Map<Integer, String> cachedCatalogs = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastCacheUpdates = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 300_000;

    private static final String SYSTEM_PROMPT = 
        "You are 'Bkeuty AI Assistant', a professional beauty expert for the Bkeuty platform.\n\n" +
        "CONTEXT ANALYSIS RULE:\n" +
        "1. INTENT CLASSIFICATION: Every time the user speaks after a recommendation, you must determine if they are:\n" +
        "   - REFINING: Adding details to the current search. (Action: Keep all previous constraints and narrow down results).\n" +
        "   - CORRECTING: Fixing a misunderstanding. (Action: Update specific constraints, keep others).\n" +
        "   - RESETTING/NEW SEARCH: Describing a completely different product or starting a new topic. (Action: Discard irrelevant old constraints, but confirm if unsure).\n" +
        "2. CUMULATIVE CONSTRAINTS: You MUST track and honor all constraints (price, brand, skin type) unless the user explicitly negates them or starts a clearly different topic.\n" +
        "3. SYNTHESIS: Before recommending, synthesize: [Historical Constraints] + [User Intent Classification] + [New Details] = [Final Balanced Decision].\n\n" +
        "DATA CONTEXT:\n" +
        "- Brands: CeraVe, La Roche-Posay, Estee Lauder, Dior, 3CE, Skin1004, Laneige, Kiehl's, Chanel, Shiseido, MAC, SK-II.\n" +
        "- Categories: Skincare (Sữa rửa mặt, Toner, Serum, Kem dưỡng, Chống nắng), Makeup (Son môi, Phấn nước/Cushion), Fragrance (Nước hoa nam/nữ).\n" +
        "- Key Options: 'Loại da' (Da nhạy cảm, Da khô, Da dầu, Mọi loại da), 'Dung tích', 'Màu sắc', 'Tone màu'.\n\n" +
        "MISSION:\n" +
        "1. Consult users based on their skin type and beauty needs while RESPECTING all previous constraints.\n" +
        "2. PRICE COMPLIANCE: If a user specifies a budget (e.g., 'under 2 million'), you MUST NOT recommend ANY product that exceeds this limit, even by a small amount. If no product fits, inform the user and suggest alternatives only if you explain they are over budget.\n" +
        "3. TIER-AWARE CONSULTING: You are aware of the user's Membership Level. If they have a discount, mention it naturally (e.g., 'As a Gold member, you get a special price on this...').\n" +
        "4. LANGUAGE CONSISTENCY: Always respond in the same language as the 'language' parameter provided in the context (vi for Vietnamese, en for English).\n" +
        "5. PROACTIVE CLARIFICATION: If the user request is vague, ask clarifying questions with specific options.\n\n" +
        "OUTPUT FORMAT (STRICT JSON ONLY):\n" +
        "Return a JSON object with this structure:\n" +
        "{\"text\": \"your_consultation_response\", \"recommendedProductId\": productId_or_null}\n\n" +
        "RULES:\n" +
        "- Use ONLY the products provided in the catalog context.\n" +
        "- If a constraint (like price) makes all catalog products unsuitable, EXPLAIN this to the user in the 'text' field and do not recommend a product that violates the constraint.\n" +
        "- DO NOT offer ordering, payment, or shipping services.\n" +
        "- NEVER say phrases like 'Bạn có muốn đặt hàng không?'.\n" +
        "- CRITICAL: The 'recommendedProductId' MUST match the specific product mentioned in your response text.\n" +
        "- IMPORTANT: Output ONLY the JSON object. Do not use markdown blocks.";

    public Map<String, Object> generateStructuredResponse(String chatHistory, String userPrompt, String language, String userId, Integer membershipLevel) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return createResponseMap("Sorry, the AI system is currently not configured. Please try again later.", null);
        }

        String targetLanguage = (language != null && language.equalsIgnoreCase("vi")) ? "Vietnamese" : "English";
        String membershipContext = "";
        if (membershipLevel != null) {
            String[] levels = {"Member", "Silver", "Gold", "Platinum", "Diamond"};
            membershipContext = "\n- USER CONTEXT: This user is a " + levels[membershipLevel] + " member. Be extra helpful and mention that prices shown are exclusive for their tier if applicable.";
        }

        String dynamicSystemPrompt = SYSTEM_PROMPT + "\n- Response must be professional and sophisticated in " + targetLanguage + "." + membershipContext;

        String productCatalog = getCachedProductCatalog(userId, membershipLevel);
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
            return objectMapper.readValue(extractedJson, new TypeReference<Map<String, Object>>() {});
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

    private String getCachedProductCatalog(String userId, Integer membershipLevel) {
        int levelKey = (membershipLevel != null) ? membershipLevel : 0;
        long currentTime = System.currentTimeMillis();
        String cached = cachedCatalogs.get(levelKey);
        Long lastUpdate = lastCacheUpdates.get(levelKey);

        if (cached == null || lastUpdate == null || (currentTime - lastUpdate) > CACHE_TTL_MS) {
            synchronized (this) {
                cached = cachedCatalogs.get(levelKey);
                lastUpdate = lastCacheUpdates.get(levelKey);
                if (cached == null || lastUpdate == null || (System.currentTimeMillis() - lastUpdate) > CACHE_TTL_MS) {
                    try {
                        String freshCatalog = productClient.getProductContext(userId, membershipLevel);
                        
                        if (freshCatalog == null || freshCatalog.isBlank() || freshCatalog.equals("[]")) {
                            cached = "[]";
                        } else {
                            JsonNode rootNode = objectMapper.readTree(freshCatalog);
                            JsonNode contentNode = rootNode.path("content");
                            JsonNode products = contentNode.isArray() ? contentNode : rootNode;
                            
                            List<Map<String, Object>> prunedList = new ArrayList<>();
                            if (products.isArray()) {
                                for (JsonNode p : products) {
                                    Map<String, Object> pruned = new HashMap<>();
                                    // Use 'id' or 'productId' depending on endpoint response structure
                                    long id = p.has("productId") ? p.path("productId").asLong() : p.path("id").asLong();
                                    pruned.put("id", id);
                                    pruned.put("name", p.path("variantName").asText());
                                    pruned.put("price", p.path("discountPrice").asDouble());
                                    prunedList.add(pruned);
                                }
                            }
                            cached = objectMapper.writeValueAsString(prunedList);
                        }
                        cachedCatalogs.put(levelKey, cached);
                        lastCacheUpdates.put(levelKey, System.currentTimeMillis());
                        log.info("Optimized product catalog cache updated for tier {}.", levelKey);
                    } catch (Exception e) {
                        log.error("Failed to update product catalog cache for tier {}: {}", levelKey, e.getMessage());
                        lastCacheUpdates.put(levelKey, System.currentTimeMillis());
                        if (cached == null) {
                            cached = "[]";
                            cachedCatalogs.put(levelKey, cached);
                        }
                    }
                }
            }
        }
        return cached;
    }
}
