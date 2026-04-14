package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bkeuty.promotion_service.util.PromotionSortUtils;
import java.time.LocalDateTime;

@RestController("userPromotionController")
@RequestMapping("/api/promotion")
@Validated
public class UserPromotionController {
    private final PromotionService promotionService;

    public UserPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponseDto>> getPromotions(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        Pageable pageable = PageRequest.of(page - 1, size, PromotionSortUtils.parseSort(sort));
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(promotionService.findAll(title, status, startAt, endAt, pageable));
    }
}
