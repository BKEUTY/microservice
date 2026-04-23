package com.bkeuty.product.controller.user;

import com.bkeuty.product.dto.user.promotion.PromotionDataRequestDto;
import com.bkeuty.product.dto.user.promotion.PromotionDataResponseDto;
import com.bkeuty.product.repository.ProductBrandRepository;
import com.bkeuty.product.repository.ProductCategoryRepository;
import com.bkeuty.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product/promotion-metadata")
public class PromotionMetadataController {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductBrandRepository productBrandRepository;

    @Autowired
    public PromotionMetadataController(ProductRepository productRepository,
                                     ProductCategoryRepository productCategoryRepository,
                                     ProductBrandRepository productBrandRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productBrandRepository = productBrandRepository;
    }

    @PostMapping
    public ResponseEntity<PromotionDataResponseDto> getPromotionMetadata(
            @RequestBody PromotionDataRequestDto request) {
        
        Map<Integer, String> productNames = new HashMap<>();
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            productRepository.findAllById(request.getProductIds())
                    .forEach(p -> productNames.put(p.getId(), p.getName()));
        }

        Map<Integer, String> categoryNames = new HashMap<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            productCategoryRepository.findAllById(request.getCategoryIds())
                    .forEach(c -> categoryNames.put(c.getId(), c.getCategoryName()));
        }

        Map<Integer, String> brandNames = new HashMap<>();
        if (request.getBrandIds() != null && !request.getBrandIds().isEmpty()) {
            productBrandRepository.findAllById(request.getBrandIds())
                    .forEach(b -> brandNames.put(b.getId(), b.getBrandName()));
        }

        return ResponseEntity.ok(PromotionDataResponseDto.builder()
                .productNames(productNames)
                .categoryNames(categoryNames)
                .brandNames(brandNames)
                .build());
    }
}
