package com.bkeuty.product.controller.internal;

import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.dto.user.cart.CartProductVariantDto;
import com.bkeuty.product.service.CartService;
import com.bkeuty.product.service.authservice.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/product/internal")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @PostMapping("/variants/batch")
    public ResponseEntity<Map<Integer, CartProductVariantDto>> getVariantsByProductIds(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody List<Integer> requestedProductIds) {
        if (!isAuthenticated(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(cartService.findDtoByProductVariantIdIn(requestedProductIds));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<CartProductVariantDto> getVariantById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable("variantId") Integer variantId) {
        if (!isAuthenticated(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(cartService.findDtoById(variantId));
    }

    private boolean isAuthenticated(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        try {
            TokenValidationResponseDto val = authService.validateToken(token);
            if (val == null || val.getUserId() == null) return false;
            String role = val.getUserRole();
            return "ADMIN".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }
}
