package com.bkeuty.order.service.cart;

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

    public CartService(CartItemRepository cartItemRepository, WebClient productWebClient) {
        this.cartItemRepository = cartItemRepository;
        this.productWebClient = productWebClient;
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

        Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                .uri("/api/product/internal/variants/batch")
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
                return ResponseEntity.status(HttpStatus.CREATED).body(toAddToCartResponseDTO(cartItemRepository.save(itemInCartItem)));
            }
        }

        CartItem cartItems = CartItem.builder()
                .productVariant(addToCartRequest.getProductVariantId())
                .quantity(addToCartRequest.getQuantity())
                .userId(tokenValidationResponseDto.getUserId())
                .isBuyNow(Boolean.TRUE.equals(addToCartRequest.getBuyNow()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toAddToCartResponseDTO(cartItemRepository.save(cartItems)));
    }

    public ResponseEntity<AddToCartResponseDto> minusToCart(TokenValidationResponseDto tokenValidationResponseDto, Integer cartItemId) {
        CartItem itemInCartItem = cartItemRepository.findByIdAndUserId(cartItemId, tokenValidationResponseDto.getUserId());

        if (itemInCartItem != null) {
            itemInCartItem.setQuantity(itemInCartItem.getQuantity() - 1);
            if (itemInCartItem.getQuantity() == 0) {
                cartItemRepository.deleteById(cartItemId);
                return ResponseEntity.status(HttpStatus.OK).body(toAddToCartResponseDTO(itemInCartItem));
            }

            return ResponseEntity.status(HttpStatus.OK).body(toAddToCartResponseDTO(cartItemRepository.save(itemInCartItem)));
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

    public AddToCartResponseDto toAddToCartResponseDTO(CartItem cartItems) {
        ProductVariantDto productVariant = productWebClient.get().uri("/api/product/internal/variant/{productVariantId}", cartItems.getProductVariant())
                .retrieve().bodyToMono(ProductVariantDto.class).block();
                
        if (productVariant == null) {
            return AddToCartResponseDto.builder()
                    .quantity(cartItems.getQuantity())
                    .productVariantId(cartItems.getProductVariant())
                    .productVariantName("Sản phẩm không còn tồn tại")
                    .build();
        }        
                
        return  AddToCartResponseDto.builder()
                .cartId(cartItems.getId())
                .quantity(cartItems.getQuantity())
                .price(productVariant.getPrice())
                .productVariantId(productVariant.getId())
                .productVariantImage(productVariant.getProductImageUrl())
                .productVariantName(productVariant.getProductVariantName())
                .promotionPrice(productVariant.getPromotionPrice())
                .build();
    }
}
