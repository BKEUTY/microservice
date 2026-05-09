package com.bkeuty.product.controller.user;

import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.DisplayProductDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.service.productservice.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bkeuty.product.util.ProductSortUtils;

import java.util.List;

@RestController("userProductController")
@RequestMapping("api/product")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<?> getHealthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping()
    public ResponseEntity<Page<DisplayProductDto>> getProducts(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page - 1, size, ProductSortUtils.parseVariantSort(sort, "id"));
        return ResponseEntity.ok(productService.getListProductVariants(pageable, search, categoryId, status));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDto> getProductById(@PathVariable Integer productId) {
        ProductDetailDto dto = productService.getProductVariantById(productId);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/name/{variantName}")
    public ResponseEntity<ProductDetailDto> getProductByName(@PathVariable String variantName) {
        ProductDetailDto dto = productService.getProductVariantByName(variantName);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(dto);
    }
}
