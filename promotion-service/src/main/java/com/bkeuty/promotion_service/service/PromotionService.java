package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckRequestDTO;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckResponseDTO;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.entity.Promotion;
import com.bkeuty.promotion_service.entity.UserVoucher;
import com.bkeuty.promotion_service.entity.VoucherPromotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import com.bkeuty.promotion_service.repository.PromotionRepository;
import com.bkeuty.promotion_service.repository.UserVoucherRepository;
import com.bkeuty.promotion_service.repository.VoucherRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final ProductPromotionRepository productPromotionRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final StringRedisTemplate redisTemplate;

    public Page<PromotionResponseDto> findAll(String title, PromotionStatus status, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        Specification<Promotion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startAt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), startAt));
            }
            if (endAt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endAt"), endAt));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return promotionRepository.findAll(spec, pageable).map(this::toDto);
    }

    private PromotionResponseDto toDto(Promotion promotion) {
        PromotionResponseDto.PromotionResponseDtoBuilder builder = PromotionResponseDto.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .createAt(promotion.getCreateAt())
                .updateAt(promotion.getUpdateAt())
                .startAt(promotion.getStartAt())
                .endAt(promotion.getEndAt())
                .status(promotion.getStatus())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .maxDiscount(promotion.getMaxDiscount())
                .promotionType(promotion.getPromotionType());

        if (promotion instanceof ProductPromotion productPromotion) {
            builder.categoryIds(productPromotion.getCategoryIds())
                .brandIds(productPromotion.getBrandIds())
                .productIds(productPromotion.getProductIds());
        } else if (promotion instanceof VoucherPromotion voucherPromotion) {
            builder.code(voucherPromotion.getCode())
                .totalQuantity(voucherPromotion.getTotalQuantity())
                .remainingQuantity(voucherPromotion.getRemainingQuantity())
                .minOrderValue(voucherPromotion.getMinOrderValue())
                .usageLimitPerUser(voucherPromotion.getUsageLimitPerUser());
        }
        return builder.build();
    }

    public BigDecimal applyVoucher(String userId, Integer voucherId, BigDecimal subtotal) {
        VoucherPromotion voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Min order value not met");
        }

        String usageKey = "voucher:" + voucherId + ":user:" + userId + ":usage";
        int userLimit = voucher.getUsageLimitPerUser() != null ? voucher.getUsageLimitPerUser() : 1;
        Long currentUsage = redisTemplate.opsForValue().increment(usageKey);

        if (currentUsage != null && currentUsage > userLimit) {
            redisTemplate.opsForValue().decrement(usageKey);
            throw new RuntimeException("User limit exceeded");
        }

        if (voucher.getRemainingQuantity() <= 0) {
            redisTemplate.opsForValue().decrement(usageKey);
            throw new RuntimeException("Voucher out of stock");
        }

        voucher.setRemainingQuantity(voucher.getRemainingQuantity() - 1);
        voucherRepository.save(voucher);

        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(BigDecimal.valueOf(voucher.getDiscountValue())).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (voucher.getMaxDiscount() != null && discount.compareTo(BigDecimal.valueOf(voucher.getMaxDiscount())) > 0) {
                discount = BigDecimal.valueOf(voucher.getMaxDiscount());
            }
        } else {
            discount = BigDecimal.valueOf(voucher.getDiscountValue());
        }
        return discount.compareTo(subtotal) > 0 ? subtotal : discount;
    }

    @Transactional
    public void refundVoucher(String userId, Integer voucherId) {
        String usageKey = "voucher:" + voucherId + ":user:" + userId + ":usage";
        redisTemplate.opsForValue().decrement(usageKey);

        voucherRepository.findById(voucherId).ifPresent(v -> {
            v.setRemainingQuantity(v.getRemainingQuantity() + 1);
            voucherRepository.save(v);
        });

        userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId).ifPresent(uv -> {
            if (uv.getUsageCount() > 0) {
                uv.setUsageCount(uv.getUsageCount() - 1);
                userVoucherRepository.save(uv);
            }
        });
    }

    @Transactional
    public void commitVoucherUsage(String userId, Integer voucherId) {
        VoucherPromotion voucher = voucherRepository.findById(voucherId).orElse(null);
        if (voucher == null) return;

        UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId)
                .orElse(new UserVoucher());
        userVoucher.setUserId(userId);
        userVoucher.setVoucher(voucher);
        userVoucher.setUsageCount((userVoucher.getUsageCount() == null ? 0 : userVoucher.getUsageCount()) + 1);
        userVoucherRepository.save(userVoucher);
    }

    public Map<Integer,ProductPromotionCheckResponseDTO> checkProductPromotion (List<ProductPromotionCheckRequestDTO> productPromotionCheckRequestDTOList){
        Map<Integer,ProductPromotionCheckResponseDTO> map = new HashMap<>();
        for (ProductPromotionCheckRequestDTO productPromotionCheckRequestDTO : productPromotionCheckRequestDTOList) {
            BigDecimal newPrice = getPromotionPrice(productPromotionCheckRequestDTO);
            map.put(productPromotionCheckRequestDTO.getProductVariantId(),new ProductPromotionCheckResponseDTO(newPrice));
        }
        return map;
    }

    public BigDecimal getPromotionPrice(ProductPromotionCheckRequestDTO productPromotionCheckRequestDto) {
        List<ProductPromotion> applicableProductPromotions = productPromotionRepository.findApplicablePromotions(productPromotionCheckRequestDto.getProductId(),
                                                                                                                 productPromotionCheckRequestDto.getBrandId(),
                                                                                                                 productPromotionCheckRequestDto.getCategoryIds(),
                                                                                                                 LocalDateTime.now(ZoneOffset.UTC));

        BigDecimal newPrice = productPromotionCheckRequestDto.getPrice();
        for(ProductPromotion applicableProductPromotion : applicableProductPromotions) {
            if(applicableProductPromotion.getDiscountType().equals(DiscountType.PERCENTAGE)){
                BigDecimal percentage = BigDecimal.valueOf(applicableProductPromotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
                BigDecimal discountAmount = productPromotionCheckRequestDto.getPrice().multiply(percentage);
                if(discountAmount.compareTo(BigDecimal.valueOf(applicableProductPromotion.getMaxDiscount())) > 0){
                    discountAmount = BigDecimal.valueOf(applicableProductPromotion.getMaxDiscount());
                }
                newPrice = newPrice.subtract(discountAmount);
            }
            else {
                newPrice = newPrice.subtract(BigDecimal.valueOf(applicableProductPromotion.getDiscountValue()));
            }
        }
        return newPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newPrice;
    }
}
