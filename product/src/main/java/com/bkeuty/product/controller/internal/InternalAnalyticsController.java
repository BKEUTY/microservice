package com.bkeuty.product.controller.internal;

import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.dto.internal.PerformanceAggregationResponseDto;
import com.bkeuty.product.dto.internal.VariantPerformanceDto;
import com.bkeuty.product.service.analytics.AnalyticsService;
import com.bkeuty.product.service.authservice.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/internal/analytics")
public class InternalAnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    public InternalAnalyticsController(AnalyticsService analyticsService, AuthService authService) {
        this.analyticsService = analyticsService;
        this.authService = authService;
    }

    @PostMapping("/aggregate")
    public ResponseEntity<PerformanceAggregationResponseDto> aggregatePerformance(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody List<VariantPerformanceDto> variantPerformances) {
        if (token != null && !isAdmin(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(analyticsService.aggregatePerformance(variantPerformances));
    }

    private boolean isAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        try {
            TokenValidationResponseDto val = authService.validateToken(token);
            return val != null && "admin".equalsIgnoreCase(val.getUserRole());
        } catch (Exception e) {
            return false;
        }
    }
}
