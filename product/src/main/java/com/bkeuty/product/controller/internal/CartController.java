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
            @RequestBody List<Integer> requestedProductIds) {
        return ResponseEntity.ok(cartService.findDtoByProductVariantIdIn(requestedProductIds));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<CartProductVariantDto> getVariantById(
            @PathVariable("variantId") Integer variantId) {
        return ResponseEntity.ok(cartService.findDtoById(variantId));
    }
}
