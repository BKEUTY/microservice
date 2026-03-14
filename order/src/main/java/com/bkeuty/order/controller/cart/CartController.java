package com.bkeuty.order.controller.cart;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartRequestDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.CartItemResponseDto;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.cart.CartService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }
    @GetMapping()
    public ResponseEntity<?> getCartItems(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId() == null||tokenValidationResponseDto.getUserRole()==null || !"user".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(cartService.getListCartItem(tokenValidationResponseDto));
    }
    @PostMapping()
    public ResponseEntity<?> addToCart(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody AddToCartRequestDto request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId() == null||tokenValidationResponseDto.getUserRole()==null || !"user".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return cartService.addToCart(tokenValidationResponseDto,request);
    }
}
