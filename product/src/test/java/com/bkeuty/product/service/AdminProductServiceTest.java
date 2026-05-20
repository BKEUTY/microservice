package com.bkeuty.product.service;

import com.bkeuty.product.dto.admin.AdminProductDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductRequestDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductResponseDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductResponseDto;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.exception.ProductNotFoundException;
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

    @InjectMocks
    private AdminProductService adminProductService;

    private Product mockProduct;
    private ProductBrand mockBrand;
    private ProductCategory mockCategory;

//    @BeforeEach
//    void setUp() {
//        mockBrand = new ProductBrand();
//        mockBrand.setId(1);
//        mockBrand.setBrandName("L'Oreal");
//
//        mockCategory = new ProductCategory();
//        mockCategory.setId(10);
//        mockCategory.setCategoryName("Skincare");
//
//        mockProduct = new Product();
//        mockProduct.setId(100);
//        mockProduct.setName("Sữa rửa mặt");
//        mockProduct.setDescription("Làm sạch da");
//        mockProduct.setImage("image.png");
//        mockProduct.setBrand(mockBrand);
//        mockProduct.setCategories(Set.of(mockCategory));
//    }
//
//    @Test
//    void createProduct_ShouldSaveAndReturnDto_WhenValidRequest() {
//        CreateProductRequestDto request = new CreateProductRequestDto();
//        request.setName("Sữa rửa mặt mới");
//        request.setDescription("Làm sạch da");
//        request.setImage("image2.png");
//        request.setBrandId(1);
//        request.setProductCategories(List.of(10));
//
//        when(productCategoryRepository.findById(10)).thenReturn(Optional.of(mockCategory));
//        when(productBrandRepository.findById(1)).thenReturn(Optional.of(mockBrand));
//        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
//            Product p = i.getArgument(0);
//            p.setId(101);
//            return p;
//        });
//
//        CreateProductResponseDto response = adminProductService.createProduct(request);
//
//        assertNotNull(response);
//        assertEquals(101, response.getId());
//        assertEquals("Sữa rửa mặt mới", response.getName());
//        assertEquals("L'Oreal", response.getBrandName());
//        assertTrue(response.getCategories().contains("Skincare"));
//
//        verify(productRepository, times(1)).save(any(Product.class));
//    }

//    @Test
//    void createProduct_ShouldThrowException_WhenBrandNotFound() {
//        CreateProductRequestDto request = new CreateProductRequestDto();
//        request.setBrandId(99);
//        request.setProductCategories(List.of(10));
//
//        when(productCategoryRepository.findById(10)).thenReturn(Optional.of(mockCategory));
//        when(productBrandRepository.findById(99)).thenReturn(Optional.empty());
//
//        assertThrows(RuntimeException.class, () -> {
//            adminProductService.createProduct(request);
//        });
//
//        verify(productRepository, never()).save(any(Product.class));
//    }

//    @Test
//    void updateProduct_ShouldUpdateAndReturnDto_WhenProductExists() {
//        UpdateProductRequestDto request = new UpdateProductRequestDto();
//        request.setId(100);
//        request.setName("Sữa rửa mặt updated");
//
//        when(productRepository.findById(100)).thenReturn(Optional.of(mockProduct));
//        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
//
//        UpdateProductResponseDto response = adminProductService.updateProduct(request);
//
//        assertNotNull(response);
//        assertEquals("Sữa rửa mặt updated", response.getName());
//        verify(productRepository, times(1)).save(mockProduct);
//    }
//
//    @Test
//    void updateProduct_ShouldThrowException_WhenProductNotFound() {
//        UpdateProductRequestDto request = new UpdateProductRequestDto();
//        request.setId(999);
//
//        when(productRepository.findById(999)).thenReturn(Optional.empty());
//
//        assertThrows(ProductNotFoundException.class, () -> {
//            adminProductService.updateProduct(request);
//        });
//
//        verify(productRepository, never()).save(any(Product.class));
//    }

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
