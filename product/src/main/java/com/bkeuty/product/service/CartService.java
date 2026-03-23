package com.bkeuty.product.service;

import com.bkeuty.product.dto.user.cart.ProductVariantDto;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final ProductVariantRepository productVariantRepository;
    public CartService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    public Map<Integer, ProductVariantDto> findDtoByProductVariantIdIn(List<Integer> productVariantIds) {
        if (productVariantIds == null || productVariantIds.isEmpty()) {
            return new HashMap<>(); 
        }
        List<ProductVariantDto> dtos = productVariantRepository.findDtoByProductVariantIdIn(productVariantIds);
        Map<Integer, ProductVariantDto> responseMap = new HashMap<>();
        for (ProductVariantDto dto : dtos) {
            responseMap.put(dto.getId(), dto);
        }
        for (Integer id : productVariantIds) {
            responseMap.putIfAbsent(id, null);
        }
        return responseMap;
    }

    public ProductVariantDto findDtoById(Integer productVariantId) {
        return productVariantRepository.findDtoByProductVariantId(productVariantId);
    }

}
