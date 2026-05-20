package com.bkeuty.product.service;

import com.bkeuty.product.dto.user.cart.CartProductVariantDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.microservicecommunication.PromotionService;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {
    private final ProductVariantRepository productVariantRepository;
    private final PromotionService promotionService;
    public CartService(ProductVariantRepository productVariantRepository, PromotionService promotionService) {
        this.productVariantRepository = productVariantRepository;
        this.promotionService = promotionService;
    }

    public Map<Integer, CartProductVariantDto> findDtoByProductVariantIdIn(List<Integer> productVariantIds, String userId, Integer membershipLevel) {
        if (productVariantIds == null || productVariantIds.isEmpty()) {
            return new HashMap<>(); 
        }
        List<ProductVariant> productVariantsInCart = productVariantRepository.findAllByIdIn(productVariantIds);
        Map<Integer, PromotionPriceDto> promotionPrices = promotionService.getListOfPromotionPrice(productVariantsInCart, userId, membershipLevel);

        Map<Integer, CartProductVariantDto> responseMap = new HashMap<>();
        for (ProductVariant productVariant : productVariantsInCart) {
            responseMap.put(productVariant.getId(), toCartProductVariantDto(productVariant, promotionPrices.get(productVariant.getId())));
        }
        for (Integer id : productVariantIds) {
            responseMap.putIfAbsent(id, null);
        }
        return responseMap;
    }

    public CartProductVariantDto findDtoById(Integer productVariantId, String userId, Integer membershipLevel) {
        ProductVariant  productVariant = productVariantRepository.findById(productVariantId).orElseThrow(()-> new ProductVariantNotFoundException("product variant not found id: "+productVariantId));
        PromotionPriceDto  promotionPriceDto = promotionService.getPromotionPrice(productVariant, userId, membershipLevel);
        System.out.println("promotionPriceDto:"+promotionPriceDto.getNewPrice());
        return CartProductVariantDto.builder()
                .id(productVariant.getId())
                .price(productVariant.getPrice())
                .productImageUrl(productVariant.getProductImageUrls().getFirst().getImageUrl()!=null?productVariant.getProductImageUrls().getFirst().getImageUrl():null)
                .productVariantName(productVariant.getProductVariantName())
                .productVariantDescription(productVariant.getDescription())
                .promotionPrice(promotionPriceDto.getNewPrice())
                .stockQuantity(productVariant.getStockQuantity())
                .build();
    }
    private CartProductVariantDto toCartProductVariantDto(ProductVariant productVariant, PromotionPriceDto promotionPriceDto) {
        return CartProductVariantDto.builder()
                .id(productVariant.getId())
                .price(productVariant.getPrice())
                .productImageUrl(productVariant.getProductImageUrls().getFirst().getImageUrl()!=null?productVariant.getProductImageUrls().getFirst().getImageUrl():null)
                .productVariantName(productVariant.getProductVariantName())
                .productVariantDescription(productVariant.getDescription())
                .promotionPrice(promotionPriceDto.getNewPrice())
                .stockQuantity(productVariant.getStockQuantity())
                .build();
    }

}
