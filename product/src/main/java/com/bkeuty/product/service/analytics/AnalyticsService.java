package com.bkeuty.product.service.analytics;

import com.bkeuty.product.dto.internal.PerformanceAggregationResponseDto;
import com.bkeuty.product.dto.internal.PerformanceResultDto;
import com.bkeuty.product.dto.internal.VariantPerformanceDto;
import com.bkeuty.product.dto.internal.VariantMappingDto;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ProductVariantRepository productVariantRepository;

    public AnalyticsService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    public PerformanceAggregationResponseDto aggregatePerformance(List<VariantPerformanceDto> variantPerformances) {
        if (variantPerformances == null || variantPerformances.isEmpty()) {
            return PerformanceAggregationResponseDto.builder()
                    .topProducts(Collections.emptyList())
                    .topBrands(Collections.emptyList())
                    .topCategories(Collections.emptyList())
                    .variantMappings(Collections.emptyMap())
                    .build();
        }

        List<Integer> variantIds = variantPerformances.stream()
                .map(VariantPerformanceDto::getVariantId)
                .collect(Collectors.toList());

        List<ProductVariant> variants = productVariantRepository.findByProductVariantIdIn(variantIds);
        Map<Integer, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        Map<Integer, PerformanceResultDto> variantMapResult = new HashMap<>();
        Map<Integer, PerformanceResultDto> brandMap = new HashMap<>();
        Map<Integer, PerformanceResultDto> categoryMap = new HashMap<>();
        Map<Integer, VariantMappingDto> variantMappings = new HashMap<>();

        for (VariantPerformanceDto vp : variantPerformances) {
            ProductVariant variant = variantMap.get(vp.getVariantId());
            if (variant == null || variant.getProduct() == null) continue;

            Product product = variant.getProduct();
            
            // Build Variant Mapping Detail
            variantMappings.computeIfAbsent(variant.getId(), k -> {
                ProductBrand brand = product.getBrand();
                List<ProductCategory> sortedCats = product.getCategories() != null 
                    ? product.getCategories().stream().sorted(Comparator.comparing(ProductCategory::getId)).toList()
                    : Collections.emptyList();
                
                ProductCategory primaryCategory = sortedCats.isEmpty() ? null : sortedCats.get(0);
                String categoryNames = sortedCats.stream()
                        .map(ProductCategory::getCategoryName)
                        .collect(Collectors.joining(", "));

                return VariantMappingDto.builder()
                        .id(variant.getId())
                        .variantName(variant.getProductVariantName())
                        .brandId(brand != null ? brand.getId() : null)
                        .brandName(brand != null ? brand.getBrandName() : null)
                        .categoryId(primaryCategory != null ? primaryCategory.getId() : null)
                        .categoryName(categoryNames.isEmpty() ? null : categoryNames)
                        .build();
            });

            PerformanceResultDto variantResult = variantMapResult.computeIfAbsent(variant.getId(), 
                k -> new PerformanceResultDto(variant.getId(), variant.getProductVariantName(), variant.getProductImageUrl(), 0L, BigDecimal.ZERO));
            variantResult.setQuantity(variantResult.getQuantity() + (vp.getQuantity() != null ? vp.getQuantity() : 0L));
            variantResult.setRevenue(variantResult.getRevenue().add(vp.getRevenue() != null ? vp.getRevenue() : BigDecimal.ZERO));
            variantResult.setProfit(variantResult.getRevenue().multiply(BigDecimal.valueOf(0.40)));

            ProductBrand brand = product.getBrand();
            if (brand != null) {
                PerformanceResultDto brandResult = brandMap.computeIfAbsent(brand.getId(), 
                    k -> new PerformanceResultDto(brand.getId(), brand.getBrandName(), brand.getImage(), 0L, BigDecimal.ZERO));
                brandResult.setQuantity(brandResult.getQuantity() + (vp.getQuantity() != null ? vp.getQuantity() : 0L));
                brandResult.setRevenue(brandResult.getRevenue().add(vp.getRevenue() != null ? vp.getRevenue() : BigDecimal.ZERO));
                brandResult.setProfit(brandResult.getRevenue().multiply(BigDecimal.valueOf(0.40)));
            }

            Set<ProductCategory> categories = product.getCategories();
            if (categories != null) {
                for (ProductCategory category : categories) {
                    if (category != null) {
                        PerformanceResultDto categoryResult = categoryMap.computeIfAbsent(category.getId(), 
                            k -> new PerformanceResultDto(category.getId(), category.getCategoryName(), null, 0L, BigDecimal.ZERO));
                        categoryResult.setQuantity(categoryResult.getQuantity() + (vp.getQuantity() != null ? vp.getQuantity() : 0L));
                        categoryResult.setRevenue(categoryResult.getRevenue().add(vp.getRevenue() != null ? vp.getRevenue() : BigDecimal.ZERO));
                        categoryResult.setProfit(categoryResult.getRevenue().multiply(BigDecimal.valueOf(0.40)));
                    }
                }
            }
        }

        List<PerformanceResultDto> topProducts = getTopResults(variantMapResult);
        List<PerformanceResultDto> topBrands = getTopResults(brandMap);
        List<PerformanceResultDto> topCategories = getTopResults(categoryMap);

        return PerformanceAggregationResponseDto.builder()
                .topProducts(topProducts)
                .topBrands(topBrands)
                .topCategories(topCategories)
                .variantMappings(variantMappings)
                .build();
    }

    private List<PerformanceResultDto> getTopResults(Map<Integer, PerformanceResultDto> map) {
        return map.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(50)
                .collect(Collectors.toList());
    }
}
