package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReviewPageResponse;
import com.bkeuty.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bkeuty.review_service.util.ReviewSortUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class PublicReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{variantId}")
    public ResponseEntity<ReviewPageResponse> getReviewsByProduct(
            @PathVariable Long variantId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) String[] sort) {
        
        Pageable pageable = PageRequest.of(page - 1, size, ReviewSortUtils.parseSort(sort, "createdAt"));
        return ResponseEntity.ok(reviewService.getReviewsByVariantId(variantId, rating, hasImage, pageable));
    }

    @GetMapping("/product/{variantId}/stats")
    public ResponseEntity<java.util.Map<String, Long>> getReviewStats(@PathVariable Long variantId) {
        return ResponseEntity.ok(reviewService.getReviewStats(variantId));
    }
}
