package com.bkeuty.product.service.productservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.DisplayProductDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.dto.user.product.ProductDto;
import com.bkeuty.product.dto.user.product.ProductOptionDto;
import com.bkeuty.product.dto.user.product.ProductVariantDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
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

    @Transactional
    public Page<DisplayProductDto> getListProductVariants(Pageable pageable, String search, Integer categoryId, String status) {
        Specification<ProductVariant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(search)) {
                String keyword = search.trim().toLowerCase();
                String searchTerm = "%" + keyword + "%";
                
                List<Predicate> searchPredicates = new ArrayList<>();
                try {
                    Integer id = Integer.parseInt(keyword);
                    searchPredicates.add(cb.equal(root.get("id"), id));
                } catch (NumberFormatException e) {}
                
                searchPredicates.add(cb.like(cb.lower(root.get("productVariantName")), searchTerm));
                searchPredicates.add(cb.like(cb.lower(root.join("product").get("name")), searchTerm));
                
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
            
            if (categoryId != null) {
                predicates.add(cb.equal(root.join("product").join("categories").get("id"), categoryId));
            }
            
            if (status != null && StringUtils.hasText(status)) {
                try {
                    predicates.add(cb.equal(root.get("status"), ProductStatus.valueOf(status.trim().toUpperCase())));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product status: " + status);
                }
            }
            
            query.distinct(true);
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductVariant> productRes = productVariantRepository.findAll(spec, pageable);
        Map<Integer, PromotionPriceDto> promotionPrice = promotionService.getListOfPromotionPrice(productRes);

        productRes.forEach(pv -> {
            if (promotionPrice.containsKey(pv.getId())) {
                BigDecimal newPrice = promotionPrice.get(pv.getId()).getNewPrice();
                if (pv.getPromotionPrice() == null || pv.getPromotionPrice().compareTo(newPrice) != 0) {
                    pv.setPromotionPrice(newPrice);
                }
            } else {
                if (pv.getPromotionPrice() == null || pv.getPromotionPrice().compareTo(pv.getPrice()) != 0) {
                    pv.setPromotionPrice(pv.getPrice());
                }
            }
        });

        return productRes.map(productVariant -> toDisplayProductDto(productVariant,promotionPrice));
    }
    private DisplayProductDto toDisplayProductDto(ProductVariant productVariant, Map<Integer,PromotionPriceDto> promotionPrice) {
        return DisplayProductDto.builder()
                .productId(productVariant.getId())
                .variantName(productVariant.getProductVariantName())
                .stockQuantity(productVariant.getStockQuantity())
                .imageUrl(productVariant.getProductImageUrl())
                .originPrice(productVariant.getPrice())
                .discountPrice(promotionPrice.get(productVariant.getId()).getNewPrice())
                .brand(productVariant.getProduct().getBrand()!=null?productVariant.getProduct().getBrand().getBrandName():null)
                .categories(productVariant.getProduct().getCategories().stream().map(this::toCategoryDto).collect(Collectors.toList()))
                .status(productVariant.getStatus().name())
                .description(productVariant.getDescription())
                .averageRating(productVariant.getAverageRating())
                .reviewCount(productVariant.getReviewCount())
                .sold(productVariant.getSold())
                .build();
    }


    @Transactional
    public ProductDetailDto getProductVariantById(Integer id) {
        ProductVariant productVariant=  productVariantRepository.findById(id).orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with id: " + id));
        PromotionPriceDto promotionPriceDto = promotionService.getPromotionPrice(productVariant);
        if (promotionPriceDto != null && promotionPriceDto.getNewPrice() != null) {
            if (productVariant.getPromotionPrice() == null || productVariant.getPromotionPrice().compareTo(promotionPriceDto.getNewPrice()) != 0) {
                productVariant.setPromotionPrice(promotionPriceDto.getNewPrice());
            }
        } else {
            if (productVariant.getPromotionPrice() == null || productVariant.getPromotionPrice().compareTo(productVariant.getPrice()) != 0) {
                productVariant.setPromotionPrice(productVariant.getPrice());
                promotionPriceDto = new PromotionPriceDto(productVariant.getPrice());
            }
        }
        return toDetailDto(productVariant.getProduct(),promotionPriceDto,productVariant);
    }

    @Transactional
    public ProductDetailDto getProductVariantByName(String variantName) {
        ProductVariant productVariant = productVariantRepository.findFirstByProductVariantName(variantName)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with name: " + variantName));
        PromotionPriceDto promotionPriceDto = promotionService.getPromotionPrice(productVariant);
        if (promotionPriceDto != null && promotionPriceDto.getNewPrice() != null) {
            if (productVariant.getPromotionPrice() == null || productVariant.getPromotionPrice().compareTo(promotionPriceDto.getNewPrice()) != 0) {
                productVariant.setPromotionPrice(promotionPriceDto.getNewPrice());
            }
        } else {
            if (productVariant.getPromotionPrice() == null || productVariant.getPromotionPrice().compareTo(productVariant.getPrice()) != 0) {
                productVariant.setPromotionPrice(productVariant.getPrice());
                promotionPriceDto = new PromotionPriceDto(productVariant.getPrice());
            }
        }
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
                .averageRating(productVariant.getAverageRating())
                .reviewCount(productVariant.getReviewCount())
                .sold(productVariant.getSold())
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
                .sold(productVariant.getSold())
                .variantOptions(options)
                .discount(promotionPrice.getNewPrice())
                .build();
    }
}
