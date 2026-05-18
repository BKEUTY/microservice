package com.bkeuty.product.service.productservice;

import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.enums.ProductStatus;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.microservicecommunication.PromotionService;
import com.bkeuty.product.repository.ProductCategoryRepository;
import com.bkeuty.product.repository.ProductOptionValueRepository;
import com.bkeuty.product.repository.ProductRepository;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductCategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private ProductService productService;

    private ProductVariant mockVariant;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        ProductBrand brand = new ProductBrand();
        brand.setBrandName("L'Oreal");

        mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setName("Sữa rửa mặt");
        mockProduct.setBrand(brand);
        mockProduct.setCategories(Collections.emptySet());

        mockVariant = new ProductVariant();
        mockVariant.setId(10);
        mockVariant.setProductVariantName("Sữa rửa mặt 50ml");
        mockVariant.setPrice(new BigDecimal("150000"));
        mockVariant.setStockQuantity(100);
        mockVariant.setStatus(ProductStatus.ACTIVE);
        mockVariant.setProduct(mockProduct);
        mockVariant.setOptionValues(Collections.emptySet());
    }

    @Test
    void getProductVariantById_ShouldReturnDetailDto_WhenVariantExists() {
        PromotionPriceDto promo = new PromotionPriceDto();
        promo.setNewPrice(new BigDecimal("130000"));
        promo.setAppliedPromotionType("PERCENTAGE");

        when(productVariantRepository.findById(10)).thenReturn(Optional.of(mockVariant));
        when(promotionService.getPromotionPrice(mockVariant, "user1", 1)).thenReturn(promo);
        when(productOptionValueRepository.findAllByOptionProductId(1)).thenReturn(Collections.emptyList());
        when(productVariantRepository.findAllByProductId(1)).thenReturn(List.of(mockVariant));

        ProductDetailDto result = productService.getProductVariantById(10, "user1", 1);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Sữa rửa mặt 50ml", result.getName());
        assertEquals(new BigDecimal("150000"), result.getOriginPrice());
        assertEquals(new BigDecimal("130000"), result.getPromotionPrice());
        assertEquals("L'Oreal", result.getBrand());

        verify(productVariantRepository, times(1)).findById(10);
    }

    @Test
    void getProductVariantById_ShouldThrowException_WhenVariantNotFound() {
        when(productVariantRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ProductVariantNotFoundException.class, () -> {
            productService.getProductVariantById(99, "user", 0);
        });
    }

    @Test
    void getProductVariantByName_ShouldReturnDetailDto_WhenVariantExists() {
        PromotionPriceDto promo = new PromotionPriceDto();
        promo.setNewPrice(new BigDecimal("150000")); // Same as origin price
        promo.setAppliedPromotionType("SERVICE_DOWN"); // Fallback

        when(productVariantRepository.findFirstByProductVariantName("Sữa rửa mặt 50ml")).thenReturn(Optional.of(mockVariant));
        when(promotionService.getPromotionPrice(mockVariant, "user1", 1)).thenReturn(promo);
        when(productOptionValueRepository.findAllByOptionProductId(1)).thenReturn(Collections.emptyList());
        when(productVariantRepository.findAllByProductId(1)).thenReturn(List.of(mockVariant));

        ProductDetailDto result = productService.getProductVariantByName("Sữa rửa mặt 50ml", "user1", 1);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertNull(result.getAppliedPromotionType()); // Null due to service down fallback
        verify(productVariantRepository, times(1)).findFirstByProductVariantName("Sữa rửa mặt 50ml");
    }

    @Test
    void getAllCategories_ShouldReturnDtoList() {
        ProductCategory cat = new ProductCategory();
        cat.setId(1);
        cat.setCategoryName("Skincare");

        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<CategoryDto> result = productService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Skincare", result.get(0).getCategoryName());
    }
}
