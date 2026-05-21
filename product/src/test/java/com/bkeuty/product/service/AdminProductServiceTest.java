package com.bkeuty.product.service;

import com.bkeuty.product.dto.admin.AdminProductDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductRequestDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductResponseDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductResponseDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantResponseDto;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.exception.ProductNotFoundException;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductBrandRepository productBrandRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AdminProductService adminProductService;

    private Product mockProduct;
    private ProductBrand mockBrand;
    private ProductCategory mockCategory;
    private ProductVariant mockVariant;

    @BeforeEach
    void setUp() {
        mockBrand = new ProductBrand();
        mockBrand.setId(1);
        mockBrand.setBrandName("L'Oreal");

        mockCategory = new ProductCategory();
        mockCategory.setId(10);
        mockCategory.setCategoryName("Skincare");

        mockProduct = new Product();
        mockProduct.setId(100);
        mockProduct.setName("Sữa rửa mặt");
        mockProduct.setDescription("Làm sạch da");
        mockProduct.setImages(new ArrayList<>());
        mockProduct.setBrand(mockBrand);
        mockProduct.setCategories(Set.of(mockCategory));

        mockVariant = new ProductVariant();
        mockVariant.setId(200);
        mockVariant.setProductVariantName("Variant 1");
        mockVariant.setPrice(BigDecimal.valueOf(100));
        mockVariant.setStockQuantity(10);
        mockVariant.setProduct(mockProduct);
        mockVariant.setProductImageUrls(new ArrayList<>());
        mockVariant.setOptionValues(Collections.emptySet());
    }

    @Test
    void createProduct_ShouldSaveAndReturnDto_WhenValidRequest() {
        CreateProductRequestDto request = new CreateProductRequestDto();
        request.setName("Sữa rửa mặt mới");
        request.setDescription("Làm sạch da");
        request.setBrandId(1);
        request.setProductCategories(List.of(10));

        when(productCategoryRepository.findById(10)).thenReturn(Optional.of(mockCategory));
        when(productBrandRepository.findById(1)).thenReturn(Optional.of(mockBrand));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(mockProduct);
        when(s3Service.uploadProductImages(any(), any())).thenReturn(Collections.emptyList());

        CreateProductResponseDto response = adminProductService.createProduct(request, Collections.emptyList());

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals("Sữa rửa mặt", response.getName());
        assertEquals("L'Oreal", response.getBrandName());
        assertTrue(response.getCategories().contains("Skincare"));

        verify(productRepository, times(1)).saveAndFlush(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowException_WhenBrandNotFound() {
        CreateProductRequestDto request = new CreateProductRequestDto();
        request.setBrandId(99);
        request.setProductCategories(List.of(10));

        when(productCategoryRepository.findById(10)).thenReturn(Optional.of(mockCategory));
        when(productBrandRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            adminProductService.createProduct(request, Collections.emptyList());
        });

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnDto_WhenProductExists() {
        UpdateProductRequestDto request = new UpdateProductRequestDto();
        request.setId(100);
        request.setName("Sữa rửa mặt updated");
        request.setImageUrl(new ArrayList<>());

        when(productRepository.findById(100)).thenReturn(Optional.of(mockProduct));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(mockProduct);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(s3Service.uploadProductImages(any(), any())).thenReturn(Collections.emptyList());

        UpdateProductResponseDto response = adminProductService.updateProduct(request, Collections.emptyList());

        assertNotNull(response);
        verify(productRepository, times(1)).saveAndFlush(mockProduct);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductNotFound() {
        UpdateProductRequestDto request = new UpdateProductRequestDto();
        request.setId(999);

        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            adminProductService.updateProduct(request, Collections.emptyList());
        });

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void updateProductVariant_ShouldUpdateAndReturnDto_WhenVariantExists() {
        UpdateProductVariantRequestDto request = new UpdateProductVariantRequestDto();
        request.setId(200);
        request.setProductVariantName("Variant Updated");
        request.setProductImageUrl(new ArrayList<>());

        when(productVariantRepository.findById(200)).thenReturn(Optional.of(mockVariant));
        when(productVariantRepository.saveAndFlush(any(ProductVariant.class))).thenReturn(mockVariant);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(mockVariant);
        when(s3Service.uploadVariantImages(any(), any())).thenReturn(Collections.emptyList());

        UpdateProductVariantResponseDto response = adminProductService.updateProductVariant(request, Collections.emptyList());

        assertNotNull(response);
        verify(productVariantRepository, times(1)).saveAndFlush(mockVariant);
    }

    @Test
    void updateProductVariant_ShouldThrowException_WhenVariantNotFound() {
        UpdateProductVariantRequestDto request = new UpdateProductVariantRequestDto();
        request.setId(999);

        when(productVariantRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ProductVariantNotFoundException.class, () -> {
            adminProductService.updateProductVariant(request, Collections.emptyList());
        });

        verify(productVariantRepository, never()).saveAndFlush(any(ProductVariant.class));
    }

    @Test
    void getAllProducts_ShouldReturnPaginatedList() {
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct));
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        Page<AdminProductDto> result = adminProductService.getAllProducts("sữa", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Sữa rửa mặt", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }
}
