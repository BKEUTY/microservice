package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckRequestDTO;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckResponseDTO;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.entity.Promotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import com.bkeuty.promotion_service.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final ProductPromotionRepository productPromotionRepository;
    public PromotionService(PromotionRepository promotionRepository,  ProductPromotionRepository productPromotionRepository) {
        this.promotionRepository = promotionRepository;
        this.productPromotionRepository = productPromotionRepository;
    }

    public Page<PromotionResponseDto> findAll(Pageable pageable) {
        return promotionRepository.findAll(pageable).map(this::toDto);
    }
    private PromotionResponseDto toDto(Promotion promotion) {
        return PromotionResponseDto.builder()
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
                .promotionType(promotion.getPromotionType())
                .build();

    }
    public Map<Integer,ProductPromotionCheckResponseDTO> checkProductPromotion (List<ProductPromotionCheckRequestDTO> productPromotionCheckRequestDTOList){
        Map<Integer,ProductPromotionCheckResponseDTO> map = new HashMap<>();
        for (ProductPromotionCheckRequestDTO productPromotionCheckRequestDTO : productPromotionCheckRequestDTOList) {
            BigDecimal newPrice = getPromotionPrice(productPromotionCheckRequestDTO);
            map.put(productPromotionCheckRequestDTO.getProductVariantId(),new ProductPromotionCheckResponseDTO(newPrice));
        }
        return map;
    }
    public ProductPromotionCheckResponseDTO getPromotion (ProductPromotionCheckRequestDTO productPromotionCheckRequestDto){
        return new ProductPromotionCheckResponseDTO(getPromotionPrice(productPromotionCheckRequestDto));
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
        return  newPrice;

    }


}
