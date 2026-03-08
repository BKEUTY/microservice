package com.bkeuty.product.controller.user;

import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.ProductDetailDto;
import com.bkeuty.product.dto.user.product.ProductDto;
import com.bkeuty.product.service.productservice.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId) {
        Pageable pageable = PageRequest.of(page, 30);
        return ResponseEntity.ok(productService.getListProducts(pageable, name, categoryId));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDto> getProductById(@PathVariable Integer productId) {
        ProductDetailDto dto = productService.getProductDetailById(productId);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(dto);
    }
}
