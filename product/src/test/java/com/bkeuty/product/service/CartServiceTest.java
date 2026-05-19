package com.bkeuty.product.service;

import com.bkeuty.product.dto.user.cart.CartProductVariantDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.microservicecommunication.PromotionService;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private CartService cartService;

    @Test
    void findDtoByProductVariantIdIn_ShouldReturnMap_WhenIdsProvided() {
        List<Integer> ids = List.of(1, 2);
        String userId = "user123";
        Integer membershipLevel = 1;

        ProductVariant variant1 = new ProductVariant();
        variant1.setId(1);
        variant1.setPrice(new BigDecimal("100000"));
        variant1.setProductVariantName("Sản phẩm 1");

        List<ProductVariant> mockVariants = List.of(variant1);

        PromotionPriceDto promo1 = new PromotionPriceDto();
        promo1.setNewPrice(new BigDecimal("90000"));

        Map<Integer, PromotionPriceDto> mockPromotions = new HashMap<>();
        mockPromotions.put(1, promo1);

        when(productVariantRepository.findAllByIdIn(ids)).thenReturn(mockVariants);
        when(promotionService.getListOfPromotionPrice(mockVariants, userId, membershipLevel)).thenReturn(mockPromotions);

        Map<Integer, CartProductVariantDto> result = cartService.findDtoByProductVariantIdIn(ids, userId, membershipLevel);

        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertNotNull(result.get(1));
        assertEquals(new BigDecimal("100000"), result.get(1).getPrice());
        assertEquals(new BigDecimal("90000"), result.get(1).getPromotionPrice());
        
        assertNull(result.get(2)); // Missing variant should have null value
        
        verify(productVariantRepository, times(1)).findAllByIdIn(ids);
        verify(promotionService, times(1)).getListOfPromotionPrice(mockVariants, userId, membershipLevel);
    }

    @Test
    void findDtoByProductVariantIdIn_ShouldReturnEmptyMap_WhenIdsNullOrEmpty() {
        Map<Integer, CartProductVariantDto> result = cartService.findDtoByProductVariantIdIn(null, "user1", 1);
        assertTrue(result.isEmpty());

        result = cartService.findDtoByProductVariantIdIn(Collections.emptyList(), "user1", 1);
        assertTrue(result.isEmpty());
        
        verify(productVariantRepository, never()).findAllByIdIn(any());
    }

    @Test
    void findDtoById_ShouldReturnDto_WhenVariantExists() {
        Integer variantId = 10;
        String userId = "user123";
        Integer membershipLevel = 2;

        ProductVariant variant = new ProductVariant();
        variant.setId(variantId);
        variant.setPrice(new BigDecimal("200000"));
        variant.setProductVariantName("Sản phẩm 2");

        PromotionPriceDto promo = new PromotionPriceDto();
        promo.setNewPrice(new BigDecimal("180000"));

        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(promotionService.getPromotionPrice(variant, userId, membershipLevel)).thenReturn(promo);

        CartProductVariantDto result = cartService.findDtoById(variantId, userId, membershipLevel);

        assertNotNull(result);
        assertEquals(variantId, result.getId());
        assertEquals(new BigDecimal("200000"), result.getPrice());
        assertEquals(new BigDecimal("180000"), result.getPromotionPrice());
        
        verify(productVariantRepository, times(1)).findById(variantId);
    }

    @Test
    void findDtoById_ShouldThrowException_WhenVariantNotFound() {
        when(productVariantRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ProductVariantNotFoundException.class, () -> {
            cartService.findDtoById(99, "user", 1);
        });

        verify(promotionService, never()).getPromotionPrice(any(), any(), any());
    }
}
