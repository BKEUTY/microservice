package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @GetMapping("/healthcheck")
    ResponseEntity<String> getHealthCheck(){
        return  ResponseEntity.ok("ok");
    }
    @GetMapping
    public ResponseEntity<Page<PromotionResponseDto>> getPromotions(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "status", required = false) PromotionStatus status,
            @RequestParam(name = "promotionType", required = false) String promotionType,
            @RequestParam(name = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(name = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort) {
        
        Pageable pageable = PageRequest.of(page - 1, size, PromotionSortUtils.parseSort(sort));
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(promotionService.findAll(title, status, startAt, endAt, promotionType, userId, pageable));
    }
}
