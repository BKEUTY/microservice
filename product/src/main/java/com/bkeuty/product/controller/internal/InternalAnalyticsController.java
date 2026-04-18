package com.bkeuty.product.controller.internal;

import com.bkeuty.product.dto.internal.PerformanceAggregationResponseDto;
import com.bkeuty.product.dto.internal.VariantPerformanceDto;
import com.bkeuty.product.service.analytics.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/internal/analytics")
public class InternalAnalyticsController {

    private final AnalyticsService analyticsService;

    public InternalAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/aggregate")
    public ResponseEntity<PerformanceAggregationResponseDto> aggregatePerformance(
            @RequestBody List<VariantPerformanceDto> variantPerformances) {
        return ResponseEntity.ok(analyticsService.aggregatePerformance(variantPerformances));
    }
}
