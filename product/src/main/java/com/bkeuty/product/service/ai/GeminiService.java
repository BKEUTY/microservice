package com.bkeuty.product.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private static final String GEMINI_MODEL = "gemini-3.1-flash-lite-preview";
    
    @Value("${gemini.api-key}")
    private String geminiApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getProductRecommendations(String prompt) {
        String systemPrompt = "You are a professional skincare and beauty consultant. Provide personalized advice in Vietnamese.";
        return callGeminiApi(systemPrompt, prompt);
    }

    public String analyzeProduct(String productDescription) {
        String systemPrompt = "You are an expert cosmetic chemist. Analyze the product details and provide insights in Vietnamese.";
        return callGeminiApi(systemPrompt, "Product Details: " + productDescription);
    }

    public String getRankedRecommendations(String profile, String orderHistory, String cartData, String reviewData, String candidateProducts) {
        String systemPrompt = "You are the 'BKeuty AI Personal Stylist & Beauty Expert', a sophisticated recommendation engine for Bkeuty - a premium cosmetics and skincare platform.\n\n" +
                "ROLE & MISSION:\n" +
                "Your mission is to perform a deep analytical correlation between a user's unique beauty profile and our exclusive product catalog. You don't just recommend; you curate a personalized beauty journey. Your suggestions must be scientifically sound, aesthetically pleasing, and highly relevant to the user's current intent.\n\n" +
                "CONTEXTUAL ANALYSIS PROTOCOL:\n" +
                "1. DATA SYNERGY: Prioritize products based on:\n" +
                "   - HISTORICAL SATISFACTION: Items similar to those the user gave 5-star reviews.\n" +
                "   - ROUTINE COMPLETION: If the user is viewing a specific item (e.g., a Cleanser), suggest products that complete a skincare routine (e.g., Toners, Serums, or Moisturizers).\n" +
                "   - BRAND & QUALITY: Maintain brand loyalty while introducing top-tier alternatives that match the user's price sensitivity and quality expectations.\n" +
                "2. STRICT EXCLUSION: If the user is currently viewing a product (provided in the context), you MUST NOT include that specific product in your recommendations. Focus on alternatives or complementary additions.\n" +
                "3. DETERMINISTIC SELECTION: Select exactly 5 product IDs. Focus purely on the highest relevance matches.\n\n" +
                "COMMUNICATION STYLE (VIETNAMESE):\n" +
                "- Tone: Sophisticated, expert, and personalized.\n" +
                "- Style: Like a professional consultant at a luxury beauty counter.\n" +
                "- Reasoning: Explain the logical connection to their cart, history, or current view (e.g., 'Dựa trên chu trình chăm sóc da hiện tại của bạn, chúng tôi gợi ý các sản phẩm giúp tối ưu hóa hiệu quả dưỡng ẩm...').\n\n" +
                "OUTPUT FORMAT: Return ONLY a valid JSON object. No extra text.\n" +
                "{\n" +
                "  \"productIds\": [int, int, int, int, int],\n" +
                "  \"reasoning\": \"string (Vietnamese)\"\n" +
                "}";
        
        String userPrompt = String.format(
                "USER CONTEXT/PROFILE: %s\n\nORDER HISTORY: %s\n\nCART DATA: %s\n\nREVIEWS DATA: %s\n\nCANDIDATE PRODUCT VARIANTS:\n%s",
                profile, orderHistory, cartData, reviewData, candidateProducts);
        
        return callGeminiApi(systemPrompt, userPrompt);
    }

    private String callGeminiApi(String systemPrompt, String userPrompt) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return "Cấu hình AI chưa hoàn tất.";
        }

        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", 
                GEMINI_MODEL, geminiApiKey);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> systemInstruction = new HashMap<>();
            systemInstruction.put("parts", List.of(Map.of("text", systemPrompt)));
            requestBody.put("system_instruction", systemInstruction);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(Map.of("text", userPrompt)));
            requestBody.put("contents", List.of(content));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.0); // Strict determinism
            generationConfig.put("topP", 1.0);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
            String responseJson = restTemplate.postForObject(url, requestEntity, String.class);
            
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode candidatesNode = rootNode.path("candidates");
            
            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode textNode = candidatesNode.get(0).path("content").path("parts").get(0).path("text");
                if (!textNode.isMissingNode()) {
                    return textNode.asText();
                }
            }
            return "{}";
        } catch (Exception e) {
            logger.error("Gemini Error: {}", e.getMessage());
            return "{}";
        }
    }
}
