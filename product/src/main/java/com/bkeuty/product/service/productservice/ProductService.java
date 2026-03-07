package com.bkeuty.product.service.productservice;

import com.bkeuty.product.dto.user.product.ProductVariantDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductRepository;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductVariantRepository productVariantRepository;
    public ProductService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    Page<ProductVariantDto> getListProductVariants(Pageable pageable, String name, String sortType) {
            return productVariantRepository.findAll(pageable).map(this::toDto);
    }
    ProductVariantDto toDto(ProductVariant productVariant) {
        return ProductVariantDto.builder().productVariantName(productVariant.getProductVariantName())
                .id(productVariant.getId())
                .price(productVariant.getPrice())
                .productImageUrl(productVariant.getProductImageUrl())
                .stockQuantity(productVariant.getStockQuantity())
                .build();
    }
}
