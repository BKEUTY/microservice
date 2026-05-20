package com.bkeuty.product.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.bkeuty.product.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkeuty.product.dto.admin.AdminProductDto;
import com.bkeuty.product.dto.admin.AdminProductVariantDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductOptionDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductRequestDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductResponseDto;
import com.bkeuty.product.dto.admin.CreateProductDto.ProductOptionValueDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductResponseDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantResponseDto;
import com.bkeuty.product.exception.ProductNotFoundException;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.repository.ProductBrandRepository;
import com.bkeuty.product.repository.ProductCategoryRepository;
import com.bkeuty.product.repository.ProductOptionRepository;
import com.bkeuty.product.repository.ProductOptionValueRepository;
import com.bkeuty.product.repository.ProductRepository;
import com.bkeuty.product.repository.ProductVariantRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;

    private final ProductCategoryRepository productCategoryRepository;

    private final ProductOptionRepository productOptionRepository;

    private final ProductOptionValueRepository productOptionValueRepository;

    private final ProductVariantRepository productVariantRepository;
    private final ProductBrandRepository productBrandRepository;
    private final S3Service s3Service;

    public AdminProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository,
            ProductOptionRepository productOptionRepository, ProductOptionValueRepository productOptionValueRepository,
            ProductVariantRepository productVariantsRepository,
                               ProductBrandRepository productBrandRepository,  S3Service s3Service) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productOptionRepository = productOptionRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.productVariantRepository = productVariantsRepository;
        this.productBrandRepository = productBrandRepository;
        this.s3Service = s3Service;

    }

    public Map<String, Set<String>> getAllUniqueOptions() {
        List<ProductOption> allOptions = productOptionRepository.findAll();
        List<ProductOptionValue> allOptionValues = productOptionValueRepository.findAll();
        Map<String, Set<String>> result = new HashMap<>();
        for (ProductOption po : allOptions) {
            result.computeIfAbsent(po.getOptionName(), k -> new TreeSet<>());
        }
        for (ProductOptionValue pov : allOptionValues) {
            result.computeIfAbsent(pov.getOption().getOptionName(), k -> new TreeSet<>()).add(pov.getOptionValueName());
        }
        return result;
    }

    public Page<AdminProductDto> getAllProducts(String search, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String keyword = search.trim().toLowerCase();
                String searchTerm = "%" + keyword + "%";
                List<Predicate> searchPredicates = new ArrayList<>();
                try {
                    Integer id = Integer.parseInt(keyword);
                    searchPredicates.add(cb.equal(root.get("id"), id));
                } catch (NumberFormatException e) {}
                searchPredicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, pageable).map(this::toAdminProductDto);
    }

    private AdminProductDto toAdminProductDto(Product product) {
        return AdminProductDto.builder()
                .productId(product.getId())
                .name(product.getName())
                .images(product.getImages())
                .description(product.getDescription())
                .categories(product.getCategories().stream().map(ProductCategory::getCategoryName)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<AdminProductVariantDto> getAllProductVariants(Integer productId) {
        return productVariantRepository.findAllByProductId(productId).stream().map(this::toAdminProductVariantDto)
                .toList();
    }

    public Page<AdminProductVariantDto> getAllVariantsPaginated(String search, Integer categoryId, Pageable pageable, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<ProductVariant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (search != null && !search.isBlank()) {
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
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            query.distinct(true);
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productVariantRepository.findAll(spec, pageable).map(this::toAdminProductVariantDto);
    }

    private AdminProductVariantDto toAdminProductVariantDto(ProductVariant productVariant) {
        Map<String, String> options = productVariant.getOptionValues().stream()
                .collect(Collectors.toMap(
                        ov -> ov.getOption().getOptionName(),
                        ov -> ov.getOptionValueName(),
                        (existing, replacement) -> existing));

        List<String> categories = productVariant.getProduct().getCategories().stream()
                .map(ProductCategory::getCategoryName)
                .collect(Collectors.toList());

        return AdminProductVariantDto.builder()
                .id(productVariant.getId())
                .productId(productVariant.getProduct().getId())
                .productImageUrl(productVariant.getProductImageUrls().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()))
                .productName(productVariant.getProduct().getName())
                .price(productVariant.getPrice())
                .promotionPrice(productVariant.getPromotionPrice())
                .optionValues(
                        productVariant.getOptionValues().stream().map(ProductOptionValue::getOptionValueName).toList())
                .variantOptions(options)
                .status(productVariant.getStatus())
                .stockQuantity(productVariant.getStockQuantity())
                .description(productVariant.getDescription())
                .productVariantName(productVariant.getProductVariantName())
                .categories(categories)
                .build();
    }

    public CreateProductResponseDto createProduct(CreateProductRequestDto requestDto, List<MultipartFile> images) {

        Product product = Product.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription()).build();
        Set<ProductCategory> setCategories = requestDto.getProductCategories().stream()
                .map(categoryId -> productCategoryRepository.findById(categoryId).get()).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        product.setCategories(setCategories);
        if (requestDto.getBrandId() != null) {
            ProductBrand productBrand = productBrandRepository.findById(requestDto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));

            product.setBrand(productBrand);
        }
        Product saveProduct = productRepository.saveAndFlush(product);
        List<String> imageUrls = s3Service.uploadProductImages(saveProduct.getId(), images);
        if(!imageUrls.isEmpty()) {
            saveProduct.setImages(imageUrls.stream().map(ProductImage::new).collect(Collectors.toList()));
            saveProduct = productRepository.save(saveProduct);
        }
        return toCreateProductResponseDto(saveProduct);

    }

    public CreateProductResponseDto toCreateProductResponseDto(Product product) {
        CreateProductResponseDto createProductResponseDto = new CreateProductResponseDto();
        createProductResponseDto.setId(product.getId());
        createProductResponseDto.setName(product.getName());
        createProductResponseDto.setDescription(product.getDescription());
        createProductResponseDto.setImage(product.getImages());
        createProductResponseDto.setCategories(
                product.getCategories().stream().map(ProductCategory::getCategoryName).collect(Collectors.toList()));
        createProductResponseDto.setBrandName(product.getBrand()!= null ? product.getBrand().getBrandName(): null);
        return createProductResponseDto;

    }

    public List<AdminProductVariantDto> createOptionValue(CreateProductOptionDto requestDTO) {
        List<List<ProductOptionValue>> optionValues = new ArrayList<>();
        List<ProductOptionValueDto> productOptions = requestDTO.getProductOptionValues();
        Product product = productRepository.findById(requestDTO.getProductId()).get();
        for (ProductOptionValueDto option : productOptions) {
            ProductOption newOption = new ProductOption();
            newOption.setProduct(product);
            newOption.setOptionName(option.getOptionName());
            newOption = productOptionRepository.save(newOption);
            List<ProductOptionValue> productOptionValueEntities = new ArrayList<>();
            for (String optionValue : option.getOptionValues()) {

                ProductOptionValue newOptionValue = new ProductOptionValue();
                newOptionValue.setOptionValueName(optionValue);
                newOptionValue.setOption(newOption);
                newOptionValue = productOptionValueRepository.save(newOptionValue);
                productOptionValueEntities.add(newOptionValue);
            }
            optionValues.add(productOptionValueEntities);
        }
        List<List<ProductOptionValue>> variantCombinations = generateCombinations(optionValues);
        return createVariantProduct(variantCombinations, product).stream().map(this::toProductVariantDTO)
                .collect(Collectors.toList());
    }

    public List<ProductVariant> createVariantProduct(List<List<ProductOptionValue>> combinations, Product product) {
        List<ProductVariant> result = new ArrayList<>();
        for (List<ProductOptionValue> productOptionValues : combinations) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setOptionValues(new HashSet<>(productOptionValues));
            variant.setDescription(product.getDescription());
            variant.setProductImageUrls(List.of());
            String optionsSuffix = productOptionValues.stream().map(ProductOptionValue::getOptionValueName).collect(Collectors.joining(" - "));
            String variantName = optionsSuffix.isEmpty() ? product.getName() : product.getName() + " - " + optionsSuffix;
            variant.setProductVariantName(variantName);         
            result.add(productVariantRepository.save(variant));
        }
        return result;
    }

    private AdminProductVariantDto toProductVariantDTO(ProductVariant productVariant) {
        List<String> categories = productVariant.getProduct().getCategories().stream()
                .map(ProductCategory::getCategoryName)
                .collect(Collectors.toList());

        return AdminProductVariantDto.builder()
                .id(productVariant.getId())
                .productId(productVariant.getProduct().getId())
                .productImageUrl(productVariant.getProductImageUrls().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()))
                .productName(productVariant.getProduct().getName())
                .price(productVariant.getPrice())
                .promotionPrice(productVariant.getPromotionPrice())
                .optionValues(
                        productVariant.getOptionValues().stream().map(ProductOptionValue::getOptionValueName).toList())
                .status(productVariant.getStatus())
                .stockQuantity(productVariant.getStockQuantity())
                .description(productVariant.getDescription())
                .productVariantName(productVariant.getProductVariantName())
                .categories(categories)
                .build();
    }

    public List<List<ProductOptionValue>> generateCombinations(List<List<ProductOptionValue>> options) {
        List<List<ProductOptionValue>> result = new ArrayList<>();
        combine(options, 0, new ArrayList<>(), result);
        return result;
    }

    private void combine(List<List<ProductOptionValue>> options, int depth,
            List<ProductOptionValue> current,
            List<List<ProductOptionValue>> result) {
        if (depth == options.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (ProductOptionValue value : options.get(depth)) {
            current.add(value);
            combine(options, depth + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    public UpdateProductResponseDto updateProduct(UpdateProductRequestDto requestDto, List<MultipartFile> images) {

        return productRepository.findById(requestDto.getId())
                .map(products -> applyUpdateProduct(products, requestDto, images))
                .map(productRepository::save)
                .map(this::toUpdateProduct)
                .orElseThrow(() -> new ProductNotFoundException("Product Not Found"));
    }

    private Product applyUpdateProduct(Product updateProduct, UpdateProductRequestDto dto, List<MultipartFile> images) {
        Optional.ofNullable(dto.getDescription()).ifPresent(updateProduct::setDescription);
        Optional.ofNullable(dto.getName()).ifPresent(updateProduct::setName);

        if (dto.getProductCategories() != null) {
            Set<ProductCategory> categories = dto.getProductCategories().stream()
                    .map(productCategoryRepository::findById)
                    .flatMap(Optional::stream) 
                    .collect(Collectors.toSet());
            updateProduct.setCategories(categories);
        }
        updateProduct = productRepository.saveAndFlush(updateProduct);
        List<String> productImages = s3Service.uploadProductImages(updateProduct.getId(),images);
        List<String> curImages = dto.getImageUrl();
        curImages.addAll(productImages);
        updateProduct.setImages(curImages.stream().map(ProductImage::new).collect(Collectors.toList()));

        return updateProduct;
    }

    private UpdateProductResponseDto toUpdateProduct(Product product) {
        List<String> categories = product.getCategories().stream().map(ProductCategory::getCategoryName).toList();
        return UpdateProductResponseDto.builder()
                .name(product.getName())
                .productCategories(categories)
                .image(product.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()))
                .description(product.getDescription())
                .build();
    }

    public UpdateProductVariantResponseDto updateProductVariant(UpdateProductVariantRequestDto requestDto, List<MultipartFile> images) {
        return productVariantRepository.findById(requestDto.getId())
                .map(productVariant -> applyUpdateProductVariant(productVariant, requestDto, images))
                .map(productVariantRepository::save)
                .map(this::toUpdateProductVariant)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not Found"));

    }

    private ProductVariant applyUpdateProductVariant(ProductVariant productVariant,
            UpdateProductVariantRequestDto dto, List<MultipartFile> images) {
        Optional.ofNullable(dto.getProductVariantName()).ifPresent(productVariant::setProductVariantName);

        Optional.ofNullable(dto.getStockQuantity()).ifPresent(productVariant::setStockQuantity);
        Optional.ofNullable(dto.getDescription()).ifPresent(productVariant::setDescription);
        ProductVariant finalProductVariant = productVariant;
        Optional.ofNullable(dto.getPrice()).ifPresent(price -> {
            boolean noActivePromotion = finalProductVariant.getPromotionPrice() == null ||
                                        finalProductVariant.getPromotionPrice().compareTo(finalProductVariant.getPrice()) == 0;
            finalProductVariant.setPrice(price);
            if (noActivePromotion) {
                finalProductVariant.setPromotionPrice(price);
            }
        });
        Optional.ofNullable(dto.getStatus()).ifPresent(productVariant::setStatus);
        productVariant = productVariantRepository.saveAndFlush(productVariant);
        List<String> imgUrls = s3Service.uploadVariantImages(productVariant.getId(),images);
        List<String> currentUrls = productVariant.getProductImageUrls().stream().map(ProductImage::getImageUrl).toList();
        currentUrls.addAll(imgUrls);
        productVariant.setProductImageUrls(currentUrls.stream().map(ProductImage::new).collect(Collectors.toList()));
        return productVariant;
    }

    private UpdateProductVariantResponseDto toUpdateProductVariant(ProductVariant productVariant) {
        return UpdateProductVariantResponseDto.builder()
                .productVariantName(productVariant.getProductVariantName())
                .description(productVariant.getDescription())
                .productName(productVariant.getProduct().getName())
                .productImageUrl(productVariant.getProductImageUrls().stream().map(ProductImage::getImageUrl).toList())
                .price(productVariant.getPrice())
                .stockQuantity(productVariant.getStockQuantity())
                .optionValues(
                        productVariant.getOptionValues().stream().map(ProductOptionValue::getOptionValueName).toList())
                .build();
    }
}
