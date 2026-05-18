package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionResponse;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPromotionStrategyTest {

    @Mock
    private ProductPromotionRepository productPromotionRepository;

    @InjectMocks
    private ProductPromotionStrategy strategy;

    private CreateProductPromotionRequest buildRequest() {
        CreateProductPromotionRequest req = new CreateProductPromotionRequest();
        req.setTitle("Giảm giá Skincare");
        req.setDescription("Ưu đãi mùa hè");
        req.setStartAt(LocalDateTime.now());
        req.setEndAt(LocalDateTime.now().plusDays(7));
        req.setDiscountType(DiscountType.PERCENTAGE);
        req.setDiscountValue(15);
        req.setMaxDiscount(100000);
        req.setCategoryIds(Set.of(1, 2));
        req.setProductIds(new HashSet<>());
        req.setBrandIds(Set.of(10));
        req.setMembershipLevels(null);
        return req;
    }

    @Test
    void getSupportedType_ShouldReturnProduct() {
        assertEquals("PRODUCT", strategy.getSupportedType());
    }

    @Test
    void create_ShouldSaveAndReturnDto() {
        CreateProductPromotionRequest request = buildRequest();

        ProductPromotion saved = new ProductPromotion();
        saved.setId(1);
        saved.setTitle("Giảm giá Skincare");
        saved.setDiscountType(DiscountType.PERCENTAGE);
        saved.setDiscountValue(15);
        saved.setCategoryIds(Set.of(1, 2));
        saved.setBrandIds(Set.of(10));
        saved.setProductIds(new HashSet<>());

        when(productPromotionRepository.save(any(ProductPromotion.class))).thenReturn(saved);

        CreatePromotionResponse response = strategy.create(request);

        assertNotNull(response);
        assertInstanceOf(CreateProductPromotionResponse.class, response);
        CreateProductPromotionResponse dto = (CreateProductPromotionResponse) response;
        assertEquals(1, dto.getId());
        assertEquals("Giảm giá Skincare", dto.getTitle());
        assertEquals(DiscountType.PERCENTAGE, dto.getDiscountType());
        verify(productPromotionRepository, times(1)).save(any(ProductPromotion.class));
    }

    @Test
    void update_ShouldUpdateAndReturnDto_WhenPromotionExists() {
        CreateProductPromotionRequest request = new CreateProductPromotionRequest();
        request.setTitle("Updated Title");
        request.setDiscountValue(20);
        request.setCategoryIds(Set.of(3));
        request.setProductIds(new HashSet<>());
        request.setBrandIds(new HashSet<>());

        ProductPromotion existing = new ProductPromotion();
        existing.setId(1);
        existing.setTitle("Old Title");
        existing.setDiscountValue(10);
        existing.setCategoryIds(new HashSet<>(Set.of(1)));
        existing.setProductIds(new HashSet<>());
        existing.setBrandIds(new HashSet<>());

        when(productPromotionRepository.findById(1)).thenReturn(Optional.of(existing));
        when(productPromotionRepository.save(any(ProductPromotion.class))).thenAnswer(i -> i.getArgument(0));

        CreatePromotionResponse response = strategy.update(1, request);

        assertNotNull(response);
        CreateProductPromotionResponse dto = (CreateProductPromotionResponse) response;
        assertEquals("Updated Title", dto.getTitle());
        assertEquals(20, dto.getDiscountValue());
        assertTrue(dto.getCategoryIds().contains(3));
    }

    @Test
    void update_ShouldThrowEntityNotFoundException_WhenPromotionNotFound() {
        when(productPromotionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                strategy.update(999, buildRequest()));

        verify(productPromotionRepository, never()).save(any());
    }
}
