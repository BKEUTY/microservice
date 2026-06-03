package com.bkeuty.product.controller.user;

import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.dto.recommendation.RecommendationResponse;
import com.bkeuty.product.service.authservice.AuthService;
import com.bkeuty.product.service.recommendation.RecommendationService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final AuthService authService;

    public RecommendationController(RecommendationService recommendationService, AuthService authService) {
        this.recommendationService = recommendationService;
        this.authService = authService;
    }

    @GetMapping("/personalized")
    public ResponseEntity<RecommendationResponse> getPersonalizedRecommendations(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        
        String userId = null;
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            TokenValidationResponseDto tokenDto = authService.validateToken(bearerToken);
            if (tokenDto != null) {
                userId = tokenDto.getUserId();
            }
        }
        
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userId));
    }

    @GetMapping("/related")
    public ResponseEntity<RecommendationResponse> getRelatedProducts(@RequestParam(name = "productName") String productName) {
        return ResponseEntity.ok(recommendationService.getRelatedProducts(productName));
    }
}
