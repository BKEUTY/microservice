package com.bkeuty.product.controller.admin;

import com.bkeuty.product.dto.admin.*;
import com.bkeuty.product.dto.admin.CreateProductDto.*;
import com.bkeuty.product.dto.admin.UpdateProductDto.*;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.*;
import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.AdminProductService;
import com.bkeuty.product.service.authservice.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bkeuty.product.util.ProductSortUtils;

import java.util.List;

@RestController("adminProductController")
@RequestMapping("api/admin/product")
@Validated
public class ProductController {
    private final AuthService authService;
    private final AdminProductService adminProductService;

    public ProductController(AdminProductService adminProductService, AuthService authService) {
        this.adminProductService = adminProductService;
        this.authService = authService;
    }

    @GetMapping()
    public ResponseEntity<Page<AdminProductDto>> getAllProducts(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size,
            @RequestParam(required = false) String[] sort) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.getAllProducts(PageRequest.of(page - 1, size, ProductSortUtils.parseSort(sort, "id"))));
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<AdminProductVariantDto>> getAllProductVariants(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer productId) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.getAllProductVariants(productId));
    }

    @GetMapping("/variants/page")
    public ResponseEntity<Page<AdminProductVariantDto>> getAllVariantsPaginated(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size,
            @RequestParam(required = false) String[] sort) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.getAllVariantsPaginated(search, categoryId, PageRequest.of(page - 1, size, ProductSortUtils.parseSort(sort, "id"))));
    }

    @PostMapping()
    public ResponseEntity<CreateProductResponseDto> createProduct(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
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
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
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
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
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
    public ResponseEntity<List<AdminProductVariantDto>> createOption(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody CreateProductOptionDto createProductOptionDTO) {
       TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
       if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
               || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
           return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
       }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminProductService.createOptionValue(createProductOptionDTO));
    }

    @GetMapping("/options")
    public ResponseEntity<?> getAllOptions(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(adminProductService.getAllUniqueOptions());
    }
}
