package com.bkeuty.order.service.cart;

import com.bkeuty.order.service.membership.MembershipService;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartRequestDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.CartItemResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.entity.CartItem;
import com.bkeuty.order.exception.CartItemNotFound;
import com.bkeuty.order.repository.CartItemRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final WebClient productWebClient;
    private final MembershipService membershipService;

    public CartService(CartItemRepository cartItemRepository, WebClient productWebClient, MembershipService membershipService) {
        this.cartItemRepository = cartItemRepository;
        this.productWebClient = productWebClient;
        this.membershipService = membershipService;
    }

    public List<CartItemResponseDto> getListCartItem(TokenValidationResponseDto tokenValidationResponseDto) {
        List<CartItem> userCartItems = cartItemRepository.findByUserIdAndIsBuyNowFalse(tokenValidationResponseDto.getUserId());

        if (userCartItems == null || userCartItems.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> productVariantIds = userCartItems.stream()
                .map(CartItem::getProductVariant)
                .filter(Objects::nonNull)
                .toList();

        if (productVariantIds.isEmpty()) {
            return new ArrayList<>();
        }

        String userId = tokenValidationResponseDto.getUserId();
        int membershipLevel = membershipService.getMembershipLevel(userId);

        Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/product/internal/variants/batch")
                        .queryParam("userId", userId)
                        .queryParam("membershipLevel", membershipLevel)
                        .build())
                .bodyValue(productVariantIds).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {
                }).block();

        return userCartItems.stream()
                .filter(cartItem -> cartItem.getProductVariant() != null)
                .map(cartItem -> toCartItemResponseDto(cartItem, productVariants))
                .toList();
    }

    public CartItemResponseDto toCartItemResponseDto(CartItem cartItem, Map<Integer, ProductVariantDto> productVariants) {
        ProductVariantDto productVariant = productVariants != null ? productVariants.get(cartItem.getProductVariant()) : null;
        
        if (productVariant == null) {
            return CartItemResponseDto.builder()
                    .productVariantId(cartItem.getProductVariant())
                    .cartId(cartItem.getId())
                    .price(BigDecimal.ZERO)
                    .image(null)
                    .name("Sản phẩm không còn tồn tại")
                    .quantity(cartItem.getQuantity())
                    .build();
        }

        return CartItemResponseDto.builder()
                .productVariantId(cartItem.getProductVariant())
                .cartId(cartItem.getId())
                .price(productVariant.getPrice())
                .image(productVariant.getProductImageUrl())
                .name(productVariant.getProductVariantName())
                .quantity(cartItem.getQuantity())
                .promotionPrice(productVariant.getPromotionPrice())
                .build();
    }

    public ResponseEntity<AddToCartResponseDto> addToCart(TokenValidationResponseDto tokenValidationResponseDto, AddToCartRequestDto addToCartRequest) {
        if (!Boolean.TRUE.equals(addToCartRequest.getBuyNow())) {
            CartItem itemInCartItem = cartItemRepository.findByUserIdAndProductVariantAndIsBuyNowFalse(tokenValidationResponseDto.getUserId(), addToCartRequest.getProductVariantId());

            if (itemInCartItem != null) {
                itemInCartItem.setQuantity(itemInCartItem.getQuantity() + addToCartRequest.getQuantity());
                CartItem saved = cartItemRepository.save(itemInCartItem);
                return ResponseEntity.status(HttpStatus.CREATED).body(toBasicResponse(saved));
            }
        }

        CartItem cartItems = CartItem.builder()
                .productVariant(addToCartRequest.getProductVariantId())
                .quantity(addToCartRequest.getQuantity())
                .userId(tokenValidationResponseDto.getUserId())
                .isBuyNow(Boolean.TRUE.equals(addToCartRequest.getBuyNow()))
                .build();

        CartItem saved = cartItemRepository.save(cartItems);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBasicResponse(saved));
    }

    private AddToCartResponseDto toBasicResponse(CartItem cartItem) {
        return AddToCartResponseDto.builder()
                .cartId(cartItem.getId())
                .productVariantId(cartItem.getProductVariant())
                .quantity(cartItem.getQuantity())
                .build();
    }

    public ResponseEntity<AddToCartResponseDto> minusToCart(TokenValidationResponseDto tokenValidationResponseDto, Integer cartItemId) {
        CartItem itemInCartItem = cartItemRepository.findByIdAndUserId(cartItemId, tokenValidationResponseDto.getUserId());

        if (itemInCartItem != null) {
            itemInCartItem.setQuantity(itemInCartItem.getQuantity() - 1);
            if (itemInCartItem.getQuantity() == 0) {
                cartItemRepository.deleteById(cartItemId);
                return ResponseEntity.status(HttpStatus.OK).body(toBasicResponse(itemInCartItem));
            }

            CartItem saved = cartItemRepository.save(itemInCartItem);
            return ResponseEntity.status(HttpStatus.OK).body(toBasicResponse(saved));
        }

        throw new CartItemNotFound("Cart Item not found", cartItemId);

    }

    public ResponseEntity<?> deleteCartItem(TokenValidationResponseDto tokenValidationResponseDto, Integer cartItemId) {
        CartItem itemInCartItem = cartItemRepository.findByIdAndUserId(cartItemId, tokenValidationResponseDto.getUserId());

        if (itemInCartItem != null) {
            cartItemRepository.deleteById(cartItemId);
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        throw new CartItemNotFound("Cart Item not found", cartItemId);
    }

    @Transactional
    public ResponseEntity<?> deleteAllCartItems(TokenValidationResponseDto tokenValidationResponseDto) {
        cartItemRepository.deleteByUserId(tokenValidationResponseDto.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
