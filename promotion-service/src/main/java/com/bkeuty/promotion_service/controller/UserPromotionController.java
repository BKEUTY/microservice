package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.service.PromotionService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("userPromotionController")
@RequestMapping("/api/promotion")
public class UserPromotionController {
    private final PromotionService promotionService;

    public UserPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponseDto>> getPromotions(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 30);
        return ResponseEntity.status(HttpStatus.OK).body(promotionService.findAll(pageable));
    }
}
