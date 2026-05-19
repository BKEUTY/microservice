package com.bkeuty.product.service.admin;

import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoRequest;
import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoResponse;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDetailDto;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandRequestDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandResponseDto;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.enums.BrandStatus;
import com.bkeuty.product.repository.ProductBrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private ProductBrandRepository productBrandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void createBrand_ShouldSaveAndReturnDto() {
        CreateBrandDtoRequest request = new CreateBrandDtoRequest();
        request.setBrandName("Innisfree");
        request.setDescription("Korean Skincare");
        request.setImage("innisfree.jpg");

        ProductBrand mockBrand = new ProductBrand();
        mockBrand.setId(1);
        mockBrand.setBrandName("Innisfree");
        mockBrand.setDescription("Korean Skincare");
        mockBrand.setImage("innisfree.jpg");
        mockBrand.setBrandStatus(BrandStatus.ACTIVE);

        when(productBrandRepository.save(any(ProductBrand.class))).thenReturn(mockBrand);

        CreateBrandDtoResponse response = brandService.createBrand(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Innisfree", response.getBrandName());
        assertEquals(BrandStatus.ACTIVE, response.getBrandStatus());

        verify(productBrandRepository, times(1)).save(any(ProductBrand.class));
    }

    @Test
    void updateBrand_ShouldUpdateAndReturnDto_WhenBrandExists() {
        UpdateProductBrandRequestDto request = new UpdateProductBrandRequestDto();
        request.setBrandName("Innisfree Updated");

        ProductBrand existingBrand = new ProductBrand();
        existingBrand.setId(1);
        existingBrand.setBrandName("Innisfree");

        when(productBrandRepository.findById(1)).thenReturn(Optional.of(existingBrand));
        when(productBrandRepository.save(any(ProductBrand.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProductBrandResponseDto response = brandService.updateBrand(1, request);

        assertNotNull(response);
        assertEquals("Innisfree Updated", response.getBrandName());
        verify(productBrandRepository, times(1)).save(existingBrand);
    }

    @Test
    void updateBrand_ShouldThrowException_WhenBrandNotFound() {
        UpdateProductBrandRequestDto request = new UpdateProductBrandRequestDto();
        when(productBrandRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            brandService.updateBrand(99, request);
        });

        assertEquals("brand not found", exception.getMessage());
        verify(productBrandRepository, never()).save(any(ProductBrand.class));
    }

    @Test
    void deleteBrand_ShouldDelete_WhenNoExceptions() {
        doNothing().when(productBrandRepository).deleteById(1);

        assertDoesNotThrow(() -> brandService.deleteBrand(1));

        verify(productBrandRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteBrand_ShouldThrowException_WhenRepositoryFails() {
        doThrow(new RuntimeException("DB Error")).when(productBrandRepository).deleteById(99);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            brandService.deleteBrand(99);
        });

        assertTrue(exception.getMessage().contains("delete brand fail"));
    }

    @Test
    void getBrandDetail_ShouldReturnDto_WhenBrandExists() {
        ProductBrand mockBrand = new ProductBrand();
        mockBrand.setId(1);
        mockBrand.setBrandName("L'Oreal");

        when(productBrandRepository.findById(1)).thenReturn(Optional.of(mockBrand));

        BrandDetailDto result = brandService.getBrandDetail(1);

        assertNotNull(result);
        assertEquals("L'Oreal", result.getName());
    }

    @Test
    void getBrands_ShouldReturnPaginatedList() {
        ProductBrand mockBrand = new ProductBrand();
        mockBrand.setId(1);
        mockBrand.setBrandName("L'Oreal");

        Page<ProductBrand> mockPage = new PageImpl<>(List.of(mockBrand));
        when(productBrandRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        Page<BrandDto> result = brandService.getBrands("Oreal", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("L'Oreal", result.getContent().get(0).getName());
    }
}
