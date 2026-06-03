package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion/internal/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final PromotionService promotionService;

    @PostMapping("/{voucherId}/apply")
    public ResponseEntity<?> applyVoucher(
            @PathVariable(name = "voucherId") Integer voucherId,
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "membershipLevel") Integer membershipLevel,
            @RequestParam(name = "subtotal") BigDecimal subtotal) {
        try {
            BigDecimal discountAmount = promotionService.applyVoucher(userId, membershipLevel, voucherId, subtotal);
            return ResponseEntity.ok(discountAmount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
