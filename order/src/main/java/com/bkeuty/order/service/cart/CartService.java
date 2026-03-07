package com.bkeuty.order.service.cart;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartRequestDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.CartItemResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.entity.CartItem;
import com.bkeuty.order.repository.CartItemRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final WebClient productWebClient;

    public CartService(CartItemRepository cartItemRepository, WebClient productWebClient) {
        this.cartItemRepository = cartItemRepository;
        this.productWebClient = productWebClient;
    }

    public List<CartItemResponseDto> getListCartItem (TokenValidationResponseDto tokenValidationResponseDto) {
        List<CartItem> userCartItems = cartItemRepository.findByUserId(tokenValidationResponseDto.getUserId());
        List<Integer> productVariantIds = userCartItems.stream().map(CartItem::getProductVariant).toList();
        Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                .uri("/api/product/internal/variants/batch")
                .bodyValue(productVariantIds).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {
                }).block();

        return cartItemRepository.findByUserId(tokenValidationResponseDto.getUserId()).stream().map(cartItem -> toCartItemResponseDto(cartItem,productVariants)).toList();
    }
    public CartItemResponseDto toCartItemResponseDto(CartItem cartItem, Map<Integer, ProductVariantDto> productVariants) {
        /// Call to Product service to fetch item
        ProductVariantDto productVariant = productVariants.get(cartItem.getProductVariant());
        return CartItemResponseDto.builder()
                .productVariantId(cartItem.getProductVariant())
                .cartId(cartItem.getId())
                .price(productVariant.getPrice())
                .image(productVariant.getProductImageUrl())
                .name(productVariant.getProductVariantName())
                .quantity(cartItem.getQuantity())
                .build();
    }
    public ResponseEntity<AddToCartResponseDto> addToCart(TokenValidationResponseDto tokenValidationResponseDto,AddToCartRequestDto addToCartRequest) {
        CartItem itemInCartItem = cartItemRepository.findByUserIdAndProductVariant(tokenValidationResponseDto.getUserId(),addToCartRequest.getProductVariantId());

        if(itemInCartItem!= null){
            itemInCartItem.setQuantity(itemInCartItem.getQuantity()+addToCartRequest.getQuantity());
            cartItemRepository.save(itemInCartItem);
        }
//        Users user = usersRepository.findById(addToCartRequest.getUserId()).orElseThrow(()-> new UserNotFoundException("User not found"));
//        ProductVariant productVariant = productVariantsRepository.findById(addToCartRequest.getProductVariantId()).orElseThrow(() -> new ProductVariantNotFoundException("Can not find product SKU"));

        CartItem cartItems  = CartItem.builder()
                .productVariant(addToCartRequest.getProductVariantId())
                .quantity(addToCartRequest.getQuantity())
                .userId(tokenValidationResponseDto.getUserId()).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toAddToCartResponseDTO(cartItemRepository.save(cartItems)));

    }
    public AddToCartResponseDto toAddToCartResponseDTO(CartItem cartItems) {
        ProductVariantDto productVariant = productWebClient.get().uri("/api/product/internal/variant/{productVariantId}",cartItems.getProductVariant())
                .retrieve().bodyToMono(ProductVariantDto.class).block();
        return  AddToCartResponseDto.builder()
                .quantity(cartItems.getQuantity())
                .price(productVariant.getPrice())
                .productVariantId(productVariant.getId())
                .productVariantImage(productVariant.getProductImageUrl())
                .productVariantName(productVariant.getProductVariantName())
                .build();
    }
}
