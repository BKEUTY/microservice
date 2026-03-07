package com.bkeuty.product.controller.user;

import com.bkeuty.product.dto.user.cart.ProductVariantDto;
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
    public ResponseEntity<Map<Integer, ProductVariantDto>> getVariantsByProductIds(
            @RequestBody List<Integer> requestedProductIds) {

        // 1. Fetch existing variants from DB


        return ResponseEntity.ok(cartService.findDtoByProductVariantIdIn(requestedProductIds));
    }
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ProductVariantDto> getVariantById(@PathVariable("variantId") Integer variantId) {
        return ResponseEntity.ok(cartService.findDtoById(variantId));
    }

}
