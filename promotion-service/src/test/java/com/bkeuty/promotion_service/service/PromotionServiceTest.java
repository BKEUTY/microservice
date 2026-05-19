package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckRequestDTO;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckResponseDTO;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.entity.UserPromotion;
import com.bkeuty.promotion_service.entity.UserVoucher;
import com.bkeuty.promotion_service.entity.VoucherPromotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import com.bkeuty.promotion_service.repository.PromotionRepository;
import com.bkeuty.promotion_service.repository.UserPromotionRepository;
import com.bkeuty.promotion_service.repository.UserVoucherRepository;
import com.bkeuty.promotion_service.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private PromotionRepository promotionRepository;
    @Mock private ProductPromotionRepository productPromotionRepository;
    @Mock private VoucherRepository voucherRepository;
    @Mock private UserVoucherRepository userVoucherRepository;
    @Mock private UserPromotionRepository userPromotionRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PromotionService promotionService;

    private static final String USER_ID = "user-uuid-123";
    private static final Integer VOUCHER_ID = 10;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ─── getPromotionPrice ───────────────────────────────────────────────────────

    @Test
    void getPromotionPrice_ShouldApplyPercentageDiscount_WhenProductPromotionExists() {
        ProductPromotionCheckRequestDTO request = new ProductPromotionCheckRequestDTO();
        request.setProductId(1);
        request.setBrandId(1);
        request.setCategoryIds(List.of(1));
        request.setPrice(new BigDecimal("200000"));
        request.setUserId(USER_ID);
        request.setMembershipLevel(1);

        ProductPromotion promo = new ProductPromotion();
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(20); // 20%
        promo.setMaxDiscount(null);
        promo.setMembershipLevels(null); // eligible for all

        when(productPromotionRepository.findApplicablePromotions(any(), any(), any(), any()))
                .thenReturn(List.of(promo));
        when(userPromotionRepository.findApplicablePromotions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ProductPromotionCheckResponseDTO result = promotionService.getPromotionPrice(request);

        assertNotNull(result);
        // 200000 - 20% = 160000
        assertEquals(0, new BigDecimal("160000").compareTo(result.getNewPrice()));
        assertEquals("ProductPromotion", result.getAppliedPromotionType());
    }

    @Test
    void getPromotionPrice_ShouldApplyFixedDiscount_WhenAmountPromotionExists() {
        ProductPromotionCheckRequestDTO request = new ProductPromotionCheckRequestDTO();
        request.setProductId(1);
        request.setBrandId(1);
        request.setCategoryIds(List.of(1));
        request.setPrice(new BigDecimal("300000"));
        request.setUserId(USER_ID);
        request.setMembershipLevel(0);

        ProductPromotion promo = new ProductPromotion();
        promo.setDiscountType(DiscountType.AMOUNT);
        promo.setDiscountValue(50000); // flat 50,000 VND off
        promo.setMembershipLevels(null);

        when(productPromotionRepository.findApplicablePromotions(any(), any(), any(), any()))
                .thenReturn(List.of(promo));
        when(userPromotionRepository.findApplicablePromotions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ProductPromotionCheckResponseDTO result = promotionService.getPromotionPrice(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("250000"), result.getNewPrice());
    }

    @Test
    void getPromotionPrice_ShouldReturnOriginalPrice_WhenNoPromotionsFound() {
        ProductPromotionCheckRequestDTO request = new ProductPromotionCheckRequestDTO();
        request.setProductId(99);
        request.setBrandId(99);
        request.setCategoryIds(Collections.emptyList());
        request.setPrice(new BigDecimal("100000"));
        request.setUserId(USER_ID);
        request.setMembershipLevel(0);

        when(productPromotionRepository.findApplicablePromotions(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(userPromotionRepository.findApplicablePromotions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ProductPromotionCheckResponseDTO result = promotionService.getPromotionPrice(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("100000"), result.getNewPrice());
        assertNull(result.getAppliedPromotionType());
    }

    @Test
    void getPromotionPrice_ShouldNotApplyPromotion_WhenMembershipLevelNotEligible() {
        ProductPromotionCheckRequestDTO request = new ProductPromotionCheckRequestDTO();
        request.setProductId(1);
        request.setBrandId(1);
        request.setCategoryIds(List.of(1));
        request.setPrice(new BigDecimal("200000"));
        request.setUserId(USER_ID);
        request.setMembershipLevel(0); // User level 0

        ProductPromotion promo = new ProductPromotion();
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(30);
        promo.setMembershipLevels(Set.of(2, 3)); // Requires level 2 or 3

        when(productPromotionRepository.findApplicablePromotions(any(), any(), any(), any()))
                .thenReturn(List.of(promo));
        when(userPromotionRepository.findApplicablePromotions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ProductPromotionCheckResponseDTO result = promotionService.getPromotionPrice(request);

        // No discount applied since user level (0) is not in {2,3}
        assertEquals(new BigDecimal("200000"), result.getNewPrice());
        assertNull(result.getAppliedPromotionType());
    }

    // ─── applyVoucher ────────────────────────────────────────────────────────────

    @Test
    void applyVoucher_ShouldReturnPercentageDiscount_WhenAllConditionsMet() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);
        voucher.setDiscountType(DiscountType.PERCENTAGE);
        voucher.setDiscountValue(10); // 10%
        voucher.setMaxDiscount(50000);
        voucher.setMinOrderValue(new BigDecimal("100000"));
        voucher.setUsageLimitPerUser(2);
        voucher.setRemainingQuantity(5);
        voucher.setMembershipLevels(null);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(valueOperations.increment(anyString())).thenReturn(1L); // 1st use
        when(voucherRepository.save(any())).thenReturn(voucher);

        BigDecimal discount = promotionService.applyVoucher(USER_ID, 0, VOUCHER_ID, new BigDecimal("500000"));

        // 10% of 500000 = 50000, capped at maxDiscount 50000
        assertEquals(0, new BigDecimal("50000").compareTo(discount));
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void applyVoucher_ShouldReturnFixedDiscount_WhenAmountType() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);
        voucher.setDiscountType(DiscountType.AMOUNT);
        voucher.setDiscountValue(30000); // flat 30,000 VND
        voucher.setMinOrderValue(new BigDecimal("100000"));
        voucher.setUsageLimitPerUser(1);
        voucher.setRemainingQuantity(10);
        voucher.setMembershipLevels(null);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(voucherRepository.save(any())).thenReturn(voucher);

        BigDecimal discount = promotionService.applyVoucher(USER_ID, 0, VOUCHER_ID, new BigDecimal("200000"));

        assertEquals(new BigDecimal("30000"), discount);
    }

    @Test
    void applyVoucher_ShouldThrowException_WhenVoucherNotFound() {
        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                promotionService.applyVoucher(USER_ID, 0, VOUCHER_ID, new BigDecimal("200000")));

        assertEquals("Voucher not found", ex.getMessage());
    }

    @Test
    void applyVoucher_ShouldThrowException_WhenMinOrderNotMet() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);
        voucher.setMinOrderValue(new BigDecimal("500000"));
        voucher.setMembershipLevels(null);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                promotionService.applyVoucher(USER_ID, 0, VOUCHER_ID, new BigDecimal("100000")));

        assertEquals("Min order value not met", ex.getMessage());
    }

    @Test
    void applyVoucher_ShouldThrowException_WhenUserLimitExceeded() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);
        voucher.setUsageLimitPerUser(1);
        voucher.setRemainingQuantity(10);
        voucher.setMembershipLevels(null);
        voucher.setMinOrderValue(null);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(valueOperations.increment(anyString())).thenReturn(2L); // 2nd use > limit of 1
        when(valueOperations.decrement(anyString())).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                promotionService.applyVoucher(USER_ID, 0, VOUCHER_ID, new BigDecimal("200000")));

        assertEquals("User limit exceeded", ex.getMessage());
        verify(valueOperations, times(1)).decrement(anyString());
    }

    @Test
    void applyVoucher_ShouldThrowException_WhenVoucherOutOfStock() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);
        voucher.setUsageLimitPerUser(5);
        voucher.setRemainingQuantity(0); // out of stock
        voucher.setMembershipLevels(null);
        voucher.setMinOrderValue(null);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(valueOperations.decrement(anyString())).thenReturn(0L);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                promotionService.applyVoucher(USER_ID, 0, VOUCHER_ID, new BigDecimal("200000")));

        assertEquals("Voucher out of stock", ex.getMessage());
    }

    // ─── refundVoucher ────────────────────────────────────────────────────────────

    @Test
    void refundVoucher_ShouldDecrementAndRestoreStock() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);
        voucher.setRemainingQuantity(4);

        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUsageCount(1);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserIdAndVoucherId(USER_ID, VOUCHER_ID))
                .thenReturn(Optional.of(userVoucher));
        when(valueOperations.decrement(anyString())).thenReturn(0L);
        when(voucherRepository.save(any())).thenReturn(voucher);
        when(userVoucherRepository.save(any())).thenReturn(userVoucher);

        assertDoesNotThrow(() -> promotionService.refundVoucher(USER_ID, VOUCHER_ID));

        assertEquals(5, voucher.getRemainingQuantity());
        assertEquals(0, userVoucher.getUsageCount());
        verify(voucherRepository, times(1)).save(voucher);
        verify(userVoucherRepository, times(1)).save(userVoucher);
    }

    // ─── commitVoucherUsage ───────────────────────────────────────────────────────

    @Test
    void commitVoucherUsage_ShouldCreateNewUserVoucher_WhenFirstUsage() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserIdAndVoucherId(USER_ID, VOUCHER_ID))
                .thenReturn(Optional.empty());
        when(userVoucherRepository.save(any(UserVoucher.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> promotionService.commitVoucherUsage(USER_ID, VOUCHER_ID));

        verify(userVoucherRepository, times(1)).save(any(UserVoucher.class));
    }

    @Test
    void commitVoucherUsage_ShouldIncrementUsageCount_WhenVoucherUsedBefore() {
        VoucherPromotion voucher = new VoucherPromotion();
        voucher.setId(VOUCHER_ID);

        UserVoucher existing = new UserVoucher();
        existing.setUserId(USER_ID);
        existing.setUsageCount(1);

        when(voucherRepository.findById(VOUCHER_ID)).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserIdAndVoucherId(USER_ID, VOUCHER_ID))
                .thenReturn(Optional.of(existing));
        when(userVoucherRepository.save(any(UserVoucher.class))).thenAnswer(i -> i.getArgument(0));

        promotionService.commitVoucherUsage(USER_ID, VOUCHER_ID);

        assertEquals(2, existing.getUsageCount());
        verify(userVoucherRepository, times(1)).save(existing);
    }
}
