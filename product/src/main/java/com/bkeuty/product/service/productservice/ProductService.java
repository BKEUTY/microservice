package com.bkeuty.product.service.productservice;

import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.dto.user.product.ProductDto;
import com.bkeuty.product.dto.user.product.ProductVariantDto;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductCategoryRepository;
import com.bkeuty.product.repository.ProductRepository;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductService(ProductVariantRepository productVariantRepository,
            ProductCategoryRepository categoryRepository,
            ProductRepository productRepository) {
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Page<ProductDto> getListProducts(Pageable pageable, String name, Integer categoryId) {
        return productRepository.findAll(pageable).map(this::toProductDto);
    }

    public Page<ProductVariantDto> getListProductVariants(Pageable pageable, String name, Integer categoryId) {
        return productVariantRepository.findWithFilters(name, categoryId, pageable).map(this::toDto);
    }

    public ProductDetailDto getProductDetailById(Integer productId) {
        return productRepository.findById(productId).map(this::toDetailDto).orElse(null);
    }

    public ProductVariantDto getProductById(Integer id) {
        return productVariantRepository.findById(id).map(this::toDto).orElse(null);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    private ProductDto toProductDto(Product product) {
        List<ProductVariant> variants = productVariantRepository.findAllByProductId(product.getId());
        BigDecimal minPrice = variants.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .image(product.getImage())
                .minPrice(minPrice)
                .categories(product.getCategories().stream().map(this::toCategoryDto).collect(Collectors.toList()))
                .build();
    }

    private ProductDetailDto toDetailDto(Product product) {
        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .image(product.getImage())
                .categories(product.getCategories().stream().map(this::toCategoryDto).collect(Collectors.toList()))
                .variants(productVariantRepository.findAllByProductId(product.getId()).stream().map(this::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private CategoryDto toCategoryDto(ProductCategory cat) {
        return CategoryDto.builder()
                .id(cat.getId())
                .categoryName(cat.getCategoryName())
                .build();
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
