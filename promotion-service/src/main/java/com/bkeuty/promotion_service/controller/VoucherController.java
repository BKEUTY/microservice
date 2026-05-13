package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/promotion/internal/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final PromotionService promotionService;

    @PostMapping("/{voucherId}/apply")
    public ResponseEntity<?> applyVoucher(
            @PathVariable Integer voucherId,
            @RequestParam String userId,
            @RequestParam Integer membershipLevel,
            @RequestParam BigDecimal subtotal) {
        try {
            BigDecimal discountAmount = promotionService.applyVoucher(userId, membershipLevel, voucherId, subtotal);
            return ResponseEntity.ok(discountAmount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
