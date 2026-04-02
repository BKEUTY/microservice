package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReviewResponse;
import com.bkeuty.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/reviews")
@RequiredArgsConstructor
public class PublicReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{variantId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long variantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean hasImage) {
        
        return ResponseEntity.ok(reviewService.getReviewsByVariantId(variantId, page, size, rating, hasImage));
    }
}
