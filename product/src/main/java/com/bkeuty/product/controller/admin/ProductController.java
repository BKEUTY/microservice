package com.bkeuty.product.controller.admin;

import com.bkeuty.product.dto.admin.*;
import com.bkeuty.product.dto.admin.CreateProductDto.*;
import com.bkeuty.product.dto.admin.UpdateProductDto.*;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.*;
import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.AdminProductService;
import com.bkeuty.product.service.authservice.AuthService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminProductController")
@RequestMapping("api/admin/product")
public class ProductController {
    private final AuthService authService;
    private final AdminProductService adminProductService;

    public ProductController(AdminProductService adminProductService, AuthService authService) {
        this.adminProductService = adminProductService;
        this.authService = authService;
    }

    @GetMapping()
    public ResponseEntity<Page<AdminProductDto>> getAllProducts(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.getAllProducts(PageRequest.of(page, size)));
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<AdminProductVariantDto>> getAllProductVariants(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer productId) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.getAllProductVariants(productId));
    }

    @PostMapping()
    public ResponseEntity<CreateProductResponseDto> createProduct(@RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody CreateProductRequestDto createProductRequestDTO) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        CreateProductResponseDto savedProduct = adminProductService.createProduct(createProductRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping()
    public ResponseEntity<UpdateProductResponseDto> updateProduct(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody UpdateProductRequestDto updateProductRequestDTO) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.updateProduct(updateProductRequestDTO));
    }

    @PutMapping("/variants")
    public ResponseEntity<UpdateProductVariantResponseDto> updateProductVariant(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody UpdateProductVariantRequestDto updateProductVariantRequestDTO) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.updateProductVariant(updateProductVariantRequestDTO));
    }

    @PostMapping("/options")
    public ResponseEntity<List<AdminProductVariantDto>> createOption(@RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody CreateProductOptionDto createProductOptionDTO) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminProductService.createOptionValue(createProductOptionDTO));
    }
}
