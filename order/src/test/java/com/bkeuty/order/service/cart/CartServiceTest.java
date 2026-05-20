package com.bkeuty.order.service.cart;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartRequestDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.entity.CartItem;
import com.bkeuty.order.repository.CartItemRepository;
import com.bkeuty.order.service.membership.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private WebClient productWebClient;

    @Mock
    private MembershipService membershipService;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    @Mock
    private Mono<ProductVariantDto> monoMock;

    @InjectMocks
    private CartService cartService;

    private TokenValidationResponseDto tokenValidationResponseDto;

    @BeforeEach
    void setUp() {
        tokenValidationResponseDto = new TokenValidationResponseDto();
        tokenValidationResponseDto.setUserId("user123");
    }

    @Test
    void addToCart_ShouldUpdateExistingCartItem_WhenItemAlreadyInCart() {
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductVariantId(10);
        requestDto.setQuantity(2);
        requestDto.setBuyNow(false);

        CartItem existingItem = CartItem.builder()
                .id(1)
                .userId("user123")
                .productVariant(10)
                .quantity(1)
                .isBuyNow(false)
                .build();

        ProductVariantDto mockVariant = new ProductVariantDto();
        mockVariant.setId(10);
        mockVariant.setPrice(new BigDecimal("150000"));
        mockVariant.setProductVariantName("Sữa rửa mặt");
        mockVariant.setProductImageUrl("image.jpg");

        when(cartItemRepository.findByUserIdAndProductVariantAndIsBuyNowFalse("user123", 10))
                .thenReturn(existingItem);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<AddToCartResponseDto> response = cartService.addToCart(tokenValidationResponseDto, requestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AddToCartResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.getQuantity()); // 1 + 2
        assertNull(body.getProductVariantName());
        
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void addToCart_ShouldCreateNewCartItem_WhenItemNotInCart() {
        AddToCartRequestDto requestDto = new AddToCartRequestDto();
        requestDto.setProductVariantId(20);
        requestDto.setQuantity(5);
        requestDto.setBuyNow(false);

        ProductVariantDto mockVariant = new ProductVariantDto();
        mockVariant.setId(20);
        mockVariant.setPrice(new BigDecimal("200000"));
        mockVariant.setProductVariantName("Kem dưỡng");
        mockVariant.setProductImageUrl("cream.jpg");

        when(cartItemRepository.findByUserIdAndProductVariantAndIsBuyNowFalse("user123", 20))
                .thenReturn(null);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<AddToCartResponseDto> response = cartService.addToCart(tokenValidationResponseDto, requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AddToCartResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(5, body.getQuantity());
        assertNull(body.getProductVariantName());
        
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }
}
