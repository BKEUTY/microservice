package com.bkeuty.product.controller;

import com.bkeuty.product.controller.user.ProductController;
import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.DisplayProductDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.service.productservice.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getProducts_ShouldReturn200_WithProductList() throws Exception {
        DisplayProductDto dto = new DisplayProductDto();
        dto.setProductId(1);
        dto.setVariantName("Kem duong am");
        dto.setOriginPrice(new BigDecimal("250000"));

        Page<DisplayProductDto> page = new PageImpl<>(List.of(dto));
        when(productService.getListProductVariants(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/product")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].variantName", is("Kem duong am")));
    }

    @Test
    void getProductById_ShouldReturn200_WhenProductExists() throws Exception {
        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(1);
        dto.setName("Kem chong nang");

        when(productService.getProductVariantById(eq(1), any(), any())).thenReturn(dto);

        mockMvc.perform(get("/api/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kem chong nang")));
    }

    @Test
    void getProductById_ShouldReturn404_WhenProductNotFound() throws Exception {
        when(productService.getProductVariantById(eq(999), any(), any())).thenReturn(null);

        mockMvc.perform(get("/api/product/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategories_ShouldReturn200_WithCategoryList() throws Exception {
        CategoryDto cat = new CategoryDto();
        cat.setId(1);
        cat.setCategoryName("Skincare");

        when(productService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/product/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryName", is("Skincare")));
    }

    @Test
    void healthCheck_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/product/healthcheck"))
                .andExpect(status().isOk());
    }
}
