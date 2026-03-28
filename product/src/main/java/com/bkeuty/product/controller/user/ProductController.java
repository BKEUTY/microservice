package com.bkeuty.product.controller.user;

import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.DisplayProductDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.service.productservice.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userProductController")
@RequestMapping("api/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping()
    public ResponseEntity<Page<DisplayProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String sort) {

        Sort sortObj = Sort.unsorted();
        if ("price_asc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "price");
        } else if ("stock_asc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "stockQuantity");
        } else if ("stock_desc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "stockQuantity");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(productService.getListProductVariants(pageable, name, categoryId));
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
