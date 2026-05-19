package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckRequestDTO;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckResponseDTO;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.entity.Promotion;
import com.bkeuty.promotion_service.entity.UserPromotion;
import com.bkeuty.promotion_service.entity.UserVoucher;
import com.bkeuty.promotion_service.entity.VoucherPromotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import com.bkeuty.promotion_service.repository.PromotionRepository;
import com.bkeuty.promotion_service.repository.UserVoucherRepository;
import com.bkeuty.promotion_service.repository.UserPromotionRepository;
import com.bkeuty.promotion_service.repository.VoucherRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final UserPromotionRepository userPromotionRepository;

    public Page<PromotionResponseDto> findAll(String title, PromotionStatus status, LocalDateTime startAt, LocalDateTime endAt, String promotionType, String userId, Pageable pageable) {
        Specification<Promotion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (promotionType != null && !promotionType.isEmpty()) {
                predicates.add(cb.equal(root.get("promotionType"), promotionType));
            }
            if (startAt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), startAt));
            }
            if (endAt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endAt"), endAt));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Promotion> page = promotionRepository.findAll(spec, pageable);
        
        Map<Integer, Integer> voucherUsages = new HashMap<>();
        if (userId != null && page.hasContent()) {
            List<VoucherPromotion> vouchers = page.getContent().stream()
                    .filter(VoucherPromotion.class::isInstance)
                    .map(VoucherPromotion.class::cast)
                    .toList();
            
            if (!vouchers.isEmpty()) {
                List<String> keys = vouchers.stream()
                        .map(v -> "voucher:" + v.getId() + ":user:" + userId + ":usage")
                        .toList();
                
                List<String> values = redisTemplate.opsForValue().multiGet(keys);
                if (values != null) {
                    int size = Math.min(vouchers.size(), values.size());
                    for (int i = 0; i < size; i++) {
                        String val = values.get(i);
                        voucherUsages.put(vouchers.get(i).getId(), val != null ? Integer.parseInt(val) : 0);
                    }
                }
            }
        }
        return page.map(p -> this.toDto(p, userId, voucherUsages));
    }

    private PromotionResponseDto toDto(Promotion promotion, String userId) {
        return toDto(promotion, userId, null);
    }

    private PromotionResponseDto toDto(Promotion promotion, String userId, Map<Integer, Integer> voucherUsages) {
        PromotionStatus currentStatus = promotion.getStatus();
        if (currentStatus != PromotionStatus.DISABLED) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(promotion.getStartAt())) {
                currentStatus = PromotionStatus.INCOMING;
            } else if (now.isAfter(promotion.getEndAt())) {
                currentStatus = PromotionStatus.ENDED;
            } else {
                currentStatus = PromotionStatus.STARTING;
            }
        }

        PromotionResponseDto.PromotionResponseDtoBuilder builder = PromotionResponseDto.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .startAt(promotion.getStartAt())
                .endAt(promotion.getEndAt())
                .status(currentStatus)
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .maxDiscount(promotion.getMaxDiscount())
                .promotionType(promotion.getPromotionType())
                .membershipLevels(promotion.getMembershipLevels());

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
                
            if (userId != null) {
                int currentUsage = 0;
                if (voucherUsages != null && voucherUsages.containsKey(voucherPromotion.getId())) {
                    currentUsage = voucherUsages.get(voucherPromotion.getId());
                } else {
                    String usageKey = "voucher:" + voucherPromotion.getId() + ":user:" + userId + ":usage";
                    String usageStr = redisTemplate.opsForValue().get(usageKey);
                    currentUsage = usageStr != null ? Integer.parseInt(usageStr) : 0;
                }
                builder.currentUserUsage(currentUsage);
                builder.remainingUsages(Math.max(0, (voucherPromotion.getUsageLimitPerUser() != null ? voucherPromotion.getUsageLimitPerUser() : 1) - currentUsage));
            }
        } else if (promotion instanceof UserPromotion userPromotion) {
            builder.birthdayMonth(userPromotion.getBirthdayMonth())
                .userIds(userPromotion.getUserIds());
        }
        return builder.build();
    }

    public BigDecimal applyVoucher(String userId, Integer membershipLevel, Integer voucherId, BigDecimal subtotal) {
        VoucherPromotion voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (!isEligibleForMembership(voucher, membershipLevel)) {
            throw new RuntimeException("Bạn không đủ hạng thành viên để sử dụng voucher này");
        }

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
        if (productPromotionCheckRequestDTOList == null || productPromotionCheckRequestDTOList.isEmpty()) return map;

        LocalDateTime now = LocalDateTime.now();
        List<Promotion> globalUserPromotions = new ArrayList<>();
        
        ProductPromotionCheckRequestDTO first = productPromotionCheckRequestDTOList.get(0);
        if (first.getUserId() != null || first.getMembershipLevel() != null) {
            globalUserPromotions.addAll(userPromotionRepository.findApplicablePromotions(
                    first.getMembershipLevel(),
                    first.getUserId(),
                    now
            ));
        }
        List<ProductPromotion> activeProductPromotions = productPromotionRepository.findAllActivePromotions(now);

        for (ProductPromotionCheckRequestDTO request : productPromotionCheckRequestDTOList) {
            List<Promotion> allPromotions = new ArrayList<>();
            
            for (ProductPromotion p : activeProductPromotions) {
                boolean matchesProduct = p.getProductIds() == null || p.getProductIds().isEmpty() || 
                                         p.getProductIds().contains(request.getProductId());
                
                boolean matchesBrand = p.getBrandIds() == null || p.getBrandIds().isEmpty() || 
                                       (request.getBrandId() != null && p.getBrandIds().contains(request.getBrandId()));
                
                boolean matchesCategory = p.getCategoryIds() == null || p.getCategoryIds().isEmpty() || 
                                          (request.getCategoryIds() != null && request.getCategoryIds().stream().anyMatch(cid -> p.getCategoryIds().contains(cid)));
                
                if (matchesProduct && matchesBrand && matchesCategory) {
                    allPromotions.add(p);
                }
            }
            allPromotions.addAll(globalUserPromotions);
            
            map.put(request.getProductVariantId(), calculatePriceHelper(request.getPrice(), allPromotions, request.getMembershipLevel()));
        }
        return map;
    }

    public ProductPromotionCheckResponseDTO getPromotionPrice(ProductPromotionCheckRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> allPromotions = new ArrayList<>();
        allPromotions.addAll(productPromotionRepository.findApplicablePromotions(
                request.getProductId(),
                request.getBrandId(),
                request.getCategoryIds(),
                now
        ));
        if (request.getUserId() != null || request.getMembershipLevel() != null) {
            allPromotions.addAll(userPromotionRepository.findApplicablePromotions(
                    request.getMembershipLevel(),
                    request.getUserId(),
                    now
            ));
        }
        return calculatePriceHelper(request.getPrice(), allPromotions, request.getMembershipLevel());
    }

    private ProductPromotionCheckResponseDTO calculatePriceHelper(BigDecimal originalPrice, List<Promotion> allPromotions, Integer membershipLevel) {
        BigDecimal maxDiscount = BigDecimal.ZERO;
        String bestPromotionType = null;
        for (Promotion promotion : allPromotions) {
            BigDecimal currentDiscount = BigDecimal.ZERO;
            if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
                BigDecimal percentage = BigDecimal.valueOf(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
                currentDiscount = originalPrice.multiply(percentage);
                if (promotion.getMaxDiscount() != null && promotion.getMaxDiscount() > 0) {
                    BigDecimal max = BigDecimal.valueOf(promotion.getMaxDiscount());
                    if (currentDiscount.compareTo(max) > 0) {
                        currentDiscount = max;
                    }
                }
            } else if (promotion.getDiscountType() == DiscountType.AMOUNT) {
                currentDiscount = BigDecimal.valueOf(promotion.getDiscountValue());
            }

            if (currentDiscount.compareTo(maxDiscount) > 0) {
                if (isEligibleForMembership(promotion, membershipLevel)) {
                    maxDiscount = currentDiscount;
                    bestPromotionType = (promotion.getClass().getSimpleName().contains("UserPromotion")) ? "UserPromotion" : "ProductPromotion";
                }
            }
        }

        BigDecimal finalPrice = originalPrice.subtract(maxDiscount);
        finalPrice = finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
        
        if (maxDiscount.compareTo(BigDecimal.ZERO) > 0 && bestPromotionType == null) {
            bestPromotionType = "ProductPromotion";
        }
        
        return new ProductPromotionCheckResponseDTO(finalPrice, bestPromotionType);
    }

    private boolean isEligibleForMembership(Promotion promotion, Integer userLevel) {
        if (promotion.getMembershipLevels() == null || promotion.getMembershipLevels().isEmpty()) {
            return true;
        }
        return userLevel != null && promotion.getMembershipLevels().contains(userLevel);
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void autoUpdatePromotionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findAll();
        for (Promotion promotion : promotions) {
            if (promotion.getStatus() != PromotionStatus.DISABLED) {
                PromotionStatus targetStatus;
                if (now.isBefore(promotion.getStartAt())) {
                    targetStatus = PromotionStatus.INCOMING;
                } else if (now.isAfter(promotion.getEndAt())) {
                    targetStatus = PromotionStatus.ENDED;
                } else {
                    targetStatus = PromotionStatus.STARTING;
                }

                if (promotion.getStatus() != targetStatus) {
                    promotion.setStatus(targetStatus);
                    promotionRepository.save(promotion);
                }
            }
        }
    }
}
