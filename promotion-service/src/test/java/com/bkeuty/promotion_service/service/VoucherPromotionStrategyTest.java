package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateVoucherPromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.CreateVoucherPromotionResponse;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.entity.VoucherPromotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.repository.VoucherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherPromotionStrategyTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherPromotionStrategy strategy;

    private CreateVoucherPromotionRequest buildRequest() {
        CreateVoucherPromotionRequest req = new CreateVoucherPromotionRequest();
        req.setTitle("Summer Sale Voucher");
        req.setDescription("Giảm 50k đơn từ 300k");
        req.setStartAt(LocalDateTime.now());
        req.setEndAt(LocalDateTime.now().plusDays(30));
        req.setDiscountType(DiscountType.AMOUNT);
        req.setDiscountValue(50000);
        req.setCode("SUMMER50");
        req.setTotalQuantity(100);
        req.setMinOrderValue(new BigDecimal("300000"));
        req.setUsageLimitPerUser(1);
        return req;
    }

    @Test
    void getSupportedType_ShouldReturnVoucher() {
        assertEquals("VOUCHER", strategy.getSupportedType());
    }

    @Test
    void create_ShouldSaveAndReturnDto_WithCorrectRemainingQuantity() {
        CreateVoucherPromotionRequest request = buildRequest();

        VoucherPromotion saved = new VoucherPromotion();
        saved.setId(1);
        saved.setTitle("Summer Sale Voucher");
        saved.setCode("SUMMER50");
        saved.setTotalQuantity(100);
        saved.setRemainingQuantity(100); // same as total at creation
        saved.setDiscountType(DiscountType.AMOUNT);
        saved.setDiscountValue(50000);
        saved.setMinOrderValue(new BigDecimal("300000"));
        saved.setUsageLimitPerUser(1);

        when(voucherRepository.save(any(VoucherPromotion.class))).thenReturn(saved);

        CreatePromotionResponse response = strategy.create(request);

        assertNotNull(response);
        assertInstanceOf(CreateVoucherPromotionResponse.class, response);
        CreateVoucherPromotionResponse dto = (CreateVoucherPromotionResponse) response;
        assertEquals("SUMMER50", dto.getCode());
        assertEquals(100, dto.getTotalQuantity());
        assertEquals(100, dto.getRemainingQuantity());
        verify(voucherRepository, times(1)).save(any(VoucherPromotion.class));
    }

    @Test
    void update_ShouldAdjustRemainingQuantity_WhenTotalQuantityIncreased() {
        CreateVoucherPromotionRequest request = new CreateVoucherPromotionRequest();
        request.setTotalQuantity(150); // Increase from 100 to 150

        VoucherPromotion existing = new VoucherPromotion();
        existing.setId(1);
        existing.setTotalQuantity(100);
        existing.setRemainingQuantity(80); // 20 already used

        when(voucherRepository.findById(1)).thenReturn(Optional.of(existing));
        when(voucherRepository.save(any(VoucherPromotion.class))).thenAnswer(i -> i.getArgument(0));

        CreatePromotionResponse response = strategy.update(1, request);

        assertNotNull(response);
        CreateVoucherPromotionResponse dto = (CreateVoucherPromotionResponse) response;
        assertEquals(150, dto.getTotalQuantity());
        // remaining = 80 + (150-100) = 130
        assertEquals(130, dto.getRemainingQuantity());
    }

    @Test
    void update_ShouldThrowEntityNotFoundException_WhenVoucherNotFound() {
        when(voucherRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                strategy.update(999, buildRequest()));

        verify(voucherRepository, never()).save(any());
    }
}
