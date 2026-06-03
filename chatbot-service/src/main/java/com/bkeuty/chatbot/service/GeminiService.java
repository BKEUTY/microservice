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
    private static final String GEMINI_MODEL = "gemini-3.1-flash-lite";

    private final Map<Integer, String> cachedCatalogs = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastCacheUpdates = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 300_000;

    private static final String SYSTEM_PROMPT = 
        "You are 'Bkeuty AI Assistant', a premium beauty consultant for Bkeuty.\n\n" +
        "CORE BEHAVIOR (STRICT):\n" +
        "1. NO REPETITION: If the user provides information (skin type, product category, budget), you MUST acknowledge and use it immediately. NEVER ask for information already present in the 'CHAT HISTORY' or the current 'USER MESSAGE'.\n" +
        "2. BREAK THE LOOP: If a user provides specific needs (e.g., 'kem dưỡng cho da dầu'), answer immediately based on the catalog. Do not ask redundant questions about skin type or product type if they were already mentioned.\n" +
        "3. CONCISE RESPONSES: Do not repeat greetings, member status, or service descriptions in every turn. If you already greeted the user or mentioned their tier, move straight to the consultation.\n" +
        "4. OUT OF STOCK: If the 'PRODUCT CATALOG' is empty or no products match the criteria, honestly inform the user that the specific items are currently out of stock. Do not stall by asking more questions if you cannot fulfill the request from the catalog.\n\n" +
        "CONSULTATION RULES:\n" +
        "1. SOURCE OF TRUTH: Use ONLY the provided 'PRODUCT CATALOG' for recommendations. Do not mention brands or categories not present in the catalog.\n" +
        "2. PRICE COMPLIANCE: NEVER recommend products exceeding the user's budget. If a budget is 'under 2 million', any product >= 2,000,000 is strictly forbidden.\n" +
        "3. TIER BENEFITS: Mention membership perks (Gold/Silver/etc) ONLY when first introducing yourself or when explaining a specific discount on a recommended product.\n" +
        "4. LANGUAGE: Always respond in the language specified in the context (vi = Vietnamese, en = English).\n" +
        "5. NO ORDERING: Do not offer payment or shipping services. Focus only on product consultation.\n\n" +
        "OUTPUT FORMAT (STRICT JSON ONLY):\n" +
        "{\"text\": \"your_consultation_response\", \"recommendedProductId\": productId_or_null}\n" +
        "Important: Do not use markdown blocks (```json). Output ONLY the raw JSON.";

    public Map<String, Object> generateStructuredResponse(String chatHistory, String userPrompt, String language, String userId, Integer membershipLevel) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return createResponseMap("Sorry, the AI system is currently not configured. Please try again later.", null);
        }

        String targetLanguage = (language != null && language.equalsIgnoreCase("vi")) ? "Vietnamese" : "English";
        String membershipContext = "";
        if (membershipLevel != null) {
            String[] levels = {"Member", "Silver", "Gold", "Platinum", "Diamond"};
            membershipContext = "\n- USER CONTEXT: This user is a " + levels[membershipLevel] + " member. You may mention this when relevant to pricing or greetings.";
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
                                    long id = p.has("productId") ? p.path("productId").asLong() : p.path("id").asLong();
                                    pruned.put("id", id);
                                    pruned.put("name", p.path("variantName").asText());
                                    pruned.put("brand", p.path("brand").asText());
                                    pruned.put("price", p.path("discountPrice").asDouble());
                                    pruned.put("stock", p.path("stockQuantity").asInt());
                                    
                                    // Extract category names for better AI matching
                                    List<String> categories = new ArrayList<>();
                                    if (p.has("categories") && p.path("categories").isArray()) {
                                        for (JsonNode cat : p.path("categories")) {
                                            categories.add(cat.path("categoryName").asText());
                                        }
                                    }
                                    pruned.put("categories", categories);
                                    
                                    // Add a snippet of description for better skin-type matching
                                    String desc = p.path("description").asText();
                                    if (desc != null && desc.length() > 100) {
                                        desc = desc.substring(0, 100) + "...";
                                    }
                                    pruned.put("description", desc);
                                    
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
