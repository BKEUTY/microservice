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

    public String getRankedRecommendations(String profile, String orderHistory, String candidateProducts) {
        String systemPrompt = "You are a Pure Analytical Recommendation Engine. Your task is to perform a strict data-driven correlation analysis between a user's purchase history and available products.\n\n" +
                "GUIDELINES:\n" +
                "1. Data Consistency: You MUST identify the Top 5 products with the highest correlation to the provided ORDER HISTORY. Base your selection on Category matching, Brand loyalty, and complementary skincare routines.\n" +
                "2. Zero Randomness: Do not attempt to 'surprise' or 'explore'. Focus purely on the most relevant matches based on hard data.\n" +
                "3. Analytical Reasoning: Provide a professional, logical explanation in Vietnamese for why these specific products were selected based on the user's historical data.\n\n" +
                "OUTPUT FORMAT: Return ONLY a valid JSON object:\n" +
                "{\n" +
                "  \"productIds\": [int, int, int, int, int],\n" +
                "  \"reasoning\": \"string (Vietnamese - professional analytical style)\"\n" +
                "}\n" +
                "No Markdown, no extra text.";
        
        String userPrompt = String.format(
                "USER PROFILE: %s\n\nORDER HISTORY: %s\n\nCANDIDATE PRODUCT VARIANTS:\n%s",
                profile, orderHistory, candidateProducts);
        
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
