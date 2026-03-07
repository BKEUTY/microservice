package com.bkeuty.product.service;

import com.bkeuty.product.dto.admin.AdminProductVariantDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductOptionDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductRequestDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductResponseDto;
import com.bkeuty.product.dto.admin.CreateProductDto.ProductOptionValueDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductResponseDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantResponseDto;
import com.bkeuty.product.entity.*;
import com.bkeuty.product.exception.ProductNotFoundException;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;

    private final ProductCategoryRepository productCategoryRepository;

    private final ProductOptionRepository productOptionRepository;

    private final ProductOptionValueRepository productOptionValueRepository;

    private final ProductVariantRepository productVariantRepository;

    public AdminProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ProductOptionRepository productOptionRepository, ProductOptionValueRepository productOptionValueRepository, ProductVariantRepository productVariantsRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productOptionRepository = productOptionRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.productVariantRepository = productVariantsRepository;
    }
    public List<AdminProductVariantDto> getAllProductVariants(Integer productId) {
        return productVariantRepository.findAllByProductId(productId).stream().map(this::toAdminProductVariantDto).toList();
    }
    private AdminProductVariantDto toAdminProductVariantDto(ProductVariant productVariant){
        return AdminProductVariantDto.builder()
                .id(productVariant.getId())
                .productImageUrl(productVariant.getProductImageUrl())
                .productName(productVariant.getProduct().getName())
                .price(productVariant.getPrice())
                .optionValues(productVariant.getOptionValues().stream().map(ProductOptionValue::getOptionValueName).toList())
                .status(productVariant.getStatus())
                .stockQuantity(productVariant.getStockQuantity())
                .description(productVariant.getDescription())
                .productVariantName(productVariant.getProductVariantName())
                .build();
    }
    //Create product
    public CreateProductResponseDto createProduct(CreateProductRequestDto requestDto){
        Product product = Product.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .image(requestDto.getImage()).build();
        Set<ProductCategory> setCategories = requestDto.getProductCategories().stream().map(categoryId -> productCategoryRepository.findById(categoryId).get()).filter(Objects::nonNull).collect(Collectors.toSet());
        product.setCategories(setCategories);
        return  toCreateProductResponseDto(productRepository.save(product));

    }
    public CreateProductResponseDto toCreateProductResponseDto(Product product) {
        CreateProductResponseDto createProductResponseDto = new CreateProductResponseDto();
        createProductResponseDto.setId(product.getId());
        createProductResponseDto.setName(product.getName());
        createProductResponseDto.setDescription(product.getDescription());
        createProductResponseDto.setImage(product.getImage());
        createProductResponseDto.setCategories(product.getCategories().stream().map(ProductCategory::getCategoryName).collect(Collectors.toList()));
        return createProductResponseDto;

    }
    public List<AdminProductVariantDto> createOptionValue(CreateProductOptionDto requestDTO){
        List<List<ProductOptionValue>> optionValues = new ArrayList<>();
        List<ProductOptionValueDto> productOptions = requestDTO.getProductOptionValues();
        Product product = productRepository.findById(requestDTO.getProductId()).get();
        for (ProductOptionValueDto option : productOptions) {
            ProductOption newOption = new ProductOption();
            newOption.setProduct(product);
            newOption.setOptionName(option.getOptionName());
            newOption = productOptionRepository.save(newOption);
            List<ProductOptionValue> productOptionValueEntities = new ArrayList<>();
            for(String optionValue : option.getOptionValues()){

                ProductOptionValue newOptionValue = new ProductOptionValue();
                newOptionValue.setOptionValueName(optionValue);
                newOptionValue.setProduct(newOption);
                newOptionValue = productOptionValueRepository.save(newOptionValue);
                productOptionValueEntities.add(newOptionValue);
            }
            optionValues.add(productOptionValueEntities);
        }
        List<List<ProductOptionValue>> variantCombinations = generateCombinations(optionValues);
        return  createVariantProduct(variantCombinations,product).stream().map(this::toProductVariantDTO).collect(Collectors.toList());
    }
    public List<ProductVariant> createVariantProduct (List<List<ProductOptionValue>> combinations, Product product){
        List<ProductVariant> result = new  ArrayList<>();
        for (List<ProductOptionValue> productOptionValues : combinations) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            for(ProductOptionValue productOptionValue : productOptionValues){
                System.out.println("productOptionValue" + productOptionValue.getOptionValueName());
            }
            variant.setOptionValues(new HashSet<>(productOptionValues));
            variant.setDescription(product.getDescription());
            variant.setProductImageUrl(product.getImage());
            variant.setProductVariantName(product.getName());
            result.add(productVariantRepository.save(variant));
        }
        return result;
    }
    private AdminProductVariantDto toProductVariantDTO(ProductVariant productVariant){
        return AdminProductVariantDto.builder()
                .id(productVariant.getId())
                .productImageUrl(productVariant.getProductImageUrl())
                .productName(productVariant.getProduct().getName())
                .price(productVariant.getPrice())
                .optionValues(productVariant.getOptionValues().stream().map(ProductOptionValue::getOptionValueName).toList())
                .status(productVariant.getStatus())
                .stockQuantity(productVariant.getStockQuantity())
                .description(productVariant.getDescription())
                .productVariantName(productVariant.getProductVariantName())
                .build();
    }
    public List<List<ProductOptionValue>> generateCombinations (List<List<ProductOptionValue>> options){
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
    public UpdateProductResponseDto updateProduct(UpdateProductRequestDto requestDto){

        return productRepository.findById(requestDto.getId())
                .map(products -> applyUpdateProduct(products,requestDto))
                .map(productRepository::save)
                .map(this::toUpdateProduct)
                .orElseThrow(() -> new ProductNotFoundException("Product Not Found"));
    }
    private Product applyUpdateProduct(Product updateProduct, UpdateProductRequestDto dto){
        Optional.ofNullable(dto.getDescription()).ifPresent(updateProduct::setDescription);
        Optional.ofNullable(dto.getName()).ifPresent(updateProduct::setName);
        Optional.ofNullable(dto.getImage()).ifPresent(updateProduct::setImage);
        if (dto.getProductCategories() != null) {
            Set<ProductCategory> categories = dto.getProductCategories().stream()
                    .map(productCategoryRepository::findById)
                    .flatMap(Optional::stream) // Cleaner than filter + map
                    .collect(Collectors.toSet());
            updateProduct.setCategories(categories);
        }

        return updateProduct;
    }
    private UpdateProductResponseDto toUpdateProduct(Product product){
        List<String> categories = product.getCategories().stream().map(ProductCategory::getCategoryName).toList();
        return  UpdateProductResponseDto.builder()
                .name(product.getName())
                .productCategories(categories)
                .image(product.getImage())
                .description(product.getDescription())
                .build();
    }
    public UpdateProductVariantResponseDto updateProductVariant(UpdateProductVariantRequestDto requestDto){
        return productVariantRepository.findById(requestDto.getId())
                .map(productVariant -> applyUpdateProductVariant(productVariant,requestDto))
                .map(productVariantRepository::save)
                .map(this::toUpdateProductVariant)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not Found"));

    }
    private ProductVariant applyUpdateProductVariant(ProductVariant productVariant, UpdateProductVariantRequestDto dto){
        Optional.ofNullable(dto.getProductVariantName()).ifPresent(productVariant::setProductVariantName);
        Optional.ofNullable(dto.getProductImageUrl()).ifPresent(productVariant::setProductImageUrl);
        Optional.ofNullable(dto.getStockQuantity()).ifPresent(productVariant::setStockQuantity);
        Optional.ofNullable(dto.getDescription()).ifPresent(productVariant::setDescription);
        Optional.ofNullable(dto.getPrice()).ifPresent(productVariant::setPrice);
        Optional.ofNullable(dto.getStatus()).ifPresent(productVariant::setStatus);
        return productVariant;
    }
    private UpdateProductVariantResponseDto toUpdateProductVariant(ProductVariant productVariant){
        return UpdateProductVariantResponseDto.builder()
                .productVariantName(productVariant.getProductVariantName())
                .description(productVariant.getDescription())
                .productName(productVariant.getProduct().getName())
                .productImageUrl(productVariant.getProductImageUrl())
                .price(productVariant.getPrice())
                .stockQuantity(productVariant.getStockQuantity())
                .optionValues(productVariant.getOptionValues().stream().map(ProductOptionValue::getOptionValueName).toList())
                .build();
    }
}
