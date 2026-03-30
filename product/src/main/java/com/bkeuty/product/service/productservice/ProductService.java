package com.bkeuty.product.service.productservice;

import com.bkeuty.product.dto.user.product.*;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductOptionValue;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.enums.ProductStatus;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.microservicecommunication.PromotionService;
import com.bkeuty.product.repository.ProductCategoryRepository;
import com.bkeuty.product.repository.ProductOptionValueRepository;
import com.bkeuty.product.repository.ProductRepository;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final PromotionService promotionService;

    public ProductService(ProductVariantRepository productVariantRepository,
                          ProductCategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          ProductOptionValueRepository productOptionValueRepository,
                          PromotionService promotionService) {
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.promotionService = promotionService;
    }

    public Page<ProductDto> getListProducts(Pageable pageable, String name, Integer categoryId) {
        return productRepository.findAll(pageable).map(this::toProductDto);
    }

    public Page<DisplayProductDto> getListProductVariants(Pageable pageable, String name, Integer categoryId, String status) {
        String pattern = StringUtils.hasText(name) ? "%" + name.toLowerCase() + "%" : null;
        ProductStatus enumStatus = (status != null && !status.trim().isEmpty()) ? ProductStatus.valueOf(status) : null;
        Page<ProductVariant> productRes = productVariantRepository.findWithFilters(pattern, categoryId, enumStatus, pageable);
        Map<Integer, PromotionPriceDto> promotionPrice = promotionService.getListOfPromotionPrice(productRes);

        return productRes.map(productVariant -> toDisplayProductDto(productVariant,promotionPrice));
    }
    private DisplayProductDto toDisplayProductDto(ProductVariant productVariant, Map<Integer,PromotionPriceDto> promotionPrice) {
        return DisplayProductDto.builder()
                .productId(productVariant.getId())
                .variantName(productVariant.getProductVariantName())
                .stock(productVariant.getStockQuantity())
                .imageUrl(productVariant.getProductImageUrl())
                .originPrice(productVariant.getPrice())
                .discountPrice(promotionPrice.get(productVariant.getId()).getNewPrice())
                .brand(productVariant.getProduct().getBrand().getBrandName())
                .categories(productVariant.getProduct().getCategories().stream().map(this::toCategoryDto).collect(Collectors.toList()))
                .status(productVariant.getStatus().name())
                .build();
    }

//    public ProductDetailDto getProductDetailById(Integer productVariantId, PromotionPriceDto promotionPrice, ProductVariantDto  productVariantDto) {
//        return productRepository.findById(productId).map(this::toDetailDto).orElse(null);
//    }

    public ProductDetailDto getProductVariantById(Integer id) {
        ProductVariant productVariant=  productVariantRepository.findById(id).orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with id: " + id));
        PromotionPriceDto promotionPriceDto = promotionService.getPromotionPrice(productVariant);
        return toDetailDto(productVariant.getProduct(),promotionPriceDto,productVariant);
    }

    public ProductDetailDto getProductVariantByName(String variantName) {
        ProductVariant productVariant = productVariantRepository.findFirstByProductVariantName(variantName)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with name: " + variantName));
        PromotionPriceDto promotionPriceDto = promotionService.getPromotionPrice(productVariant);
        return toDetailDto(productVariant.getProduct(), promotionPriceDto, productVariant);
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

    private ProductDetailDto toDetailDto(Product product, PromotionPriceDto promotionPrice, ProductVariant productVariant) {
        List<ProductOptionValue> optionValues = productOptionValueRepository.findAllByOptionProductId(product.getId());
        List<ProductOptionDto> options = optionValues.stream()
                .collect(Collectors.groupingBy(ov -> ov.getOption().getOptionName()))
                .entrySet().stream()
                .map(e -> ProductOptionDto.builder()
                        .name(e.getKey())
                        .values(e.getValue().stream().map(ProductOptionValue::getOptionValueName).distinct().collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return ProductDetailDto.builder()
                .id(productVariant.getId())
                .name(productVariant.getProductVariantName())
                .description(productVariant.getDescription())
                .image(productVariant.getProductImageUrl())
                .originPrice(productVariant.getPrice())
                .promotionPrice(promotionPrice.getNewPrice())
                .brand(product.getBrand().getBrandName())
                .categories(product.getCategories().stream().map(this::toCategoryDto).collect(Collectors.toList()))
                .variants(productVariantRepository.findAllByProductId(product.getId()).stream().map(productVariantInstance ->toDto(productVariantInstance,new PromotionPriceDto(BigDecimal.ZERO)) )
                        .collect(Collectors.toList()))
                .options(options)
                .status(productVariant.getStatus().name())
                .build();
    }

    private CategoryDto toCategoryDto(ProductCategory cat) {
        return CategoryDto.builder()
                .id(cat.getId())
                .categoryName(cat.getCategoryName())
                .build();
    }

    ProductVariantDto toDto(ProductVariant productVariant, PromotionPriceDto promotionPrice) {
        Map<String, String> options = productVariant.getOptionValues().stream()
                .collect(Collectors.toMap(
                        ov -> ov.getOption().getOptionName(),
                        ov -> ov.getOptionValueName(),
                        (existing, replacement) -> existing
                ));

        return ProductVariantDto.builder()
                .productVariantName(productVariant.getProductVariantName())
                .id(productVariant.getId())
                .price(productVariant.getPrice())
                .productImageUrl(productVariant.getProductImageUrl())
                .stockQuantity(productVariant.getStockQuantity())
                .variantOptions(options)
                .discount(promotionPrice.getNewPrice())
                .build();
    }
}
