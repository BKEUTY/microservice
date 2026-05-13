package com.bkeuty.product.controller.internal;

import com.bkeuty.product.dto.user.cart.CartProductVariantDto;
import com.bkeuty.product.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/product/internal")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/variants/batch")
    public ResponseEntity<Map<Integer, CartProductVariantDto>> getVariantsByProductIds(
            @RequestBody List<Integer> requestedProductIds,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer membershipLevel) {
        return ResponseEntity.ok(cartService.findDtoByProductVariantIdIn(requestedProductIds, userId, membershipLevel));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<CartProductVariantDto> getVariantById(
            @PathVariable("variantId") Integer variantId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer membershipLevel) {
        return ResponseEntity.ok(cartService.findDtoById(variantId, userId, membershipLevel));
    }
}
