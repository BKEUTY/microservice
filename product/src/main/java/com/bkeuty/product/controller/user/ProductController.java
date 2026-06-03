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
import java.math.BigDecimal;
import org.springframework.web.server.ResponseStatusException;
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
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "sort", required = false) String[] sort,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "minStock", required = false) @Min(0) Integer minStock,
            @RequestParam(name = "minPrice", required = false) @Min(0) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) @Min(0) BigDecimal maxPrice,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "membershipLevel", required = false) Integer membershipLevel) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }

        Pageable pageable = PageRequest.of(page - 1, size, ProductSortUtils.parseVariantSort(sort, "id"));
        return ResponseEntity.ok(productService.getListProductVariants(pageable, search, categoryId, status, minStock, minPrice, maxPrice, userId, membershipLevel));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDto> getProductById(@PathVariable(name = "productId") Integer productId, 
                                                           @RequestParam(name = "userId", required = false) String userId,
                                                           @RequestParam(name = "membershipLevel", required = false) Integer membershipLevel) {
        ProductDetailDto dto = productService.getProductVariantById(productId, userId, membershipLevel);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/name/{variantName}")
    public ResponseEntity<ProductDetailDto> getProductByName(@PathVariable(name = "variantName") String variantName,
                                                             @RequestParam(name = "userId", required = false) String userId,
                                                             @RequestParam(name = "membershipLevel", required = false) Integer membershipLevel) {
        ProductDetailDto dto = productService.getProductVariantByName(variantName, userId, membershipLevel);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(dto);
    }
}
