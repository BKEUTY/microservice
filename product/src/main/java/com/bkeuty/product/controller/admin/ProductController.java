package com.bkeuty.product.controller.admin;

import com.bkeuty.product.dto.admin.*;
import com.bkeuty.product.dto.admin.CreateProductDto.*;
import com.bkeuty.product.dto.admin.UpdateProductDto.*;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.*;
import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.AdminProductService;
import com.bkeuty.product.service.authservice.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import com.bkeuty.product.util.ProductSortUtils;

import java.util.List;

@RestController("adminProductController")
@RequestMapping("api/admin/product")
@Validated
public class ProductController {
    private final AuthService authService;
    private final AdminProductService adminProductService;
    private final ObjectMapper objectMapper;

    public ProductController(AdminProductService adminProductService, AuthService authService,  ObjectMapper objectMapper) {
        this.adminProductService = adminProductService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @GetMapping()
    public ResponseEntity<Page<AdminProductDto>> getAllProducts(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size,
            @RequestParam(required = false) String[] sort) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.getAllProducts(search, PageRequest.of(page - 1, size, ProductSortUtils.parseProductSort(sort, "id"))));
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<AdminProductVariantDto>> getAllProductVariants(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer productId) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
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
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) @Min(0) BigDecimal minPrice,
            @RequestParam(required = false) @Min(0) BigDecimal maxPrice) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.getAllVariantsPaginated(search, categoryId, PageRequest.of(page - 1, size, ProductSortUtils.parseVariantSort(sort, "id")), minPrice, maxPrice));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateProductResponseDto> createProduct(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestPart("request") String requestJson, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        CreateProductRequestDto request;
        try {
            request = objectMapper.readValue(requestJson, CreateProductRequestDto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println(requestJson);
        System.out.println(request);
        CreateProductResponseDto savedProduct = adminProductService.createProduct(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProductResponseDto> updateProduct(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestPart("request") String requestJson, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        UpdateProductRequestDto request;
        try{
            request = objectMapper.readValue(requestJson, UpdateProductRequestDto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.updateProduct(request, images));
    }

    @PutMapping(path = "/variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProductVariantResponseDto> updateProductVariant(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestPart("request") String requestJson, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        UpdateProductVariantRequestDto updateProductRequestDto;
        try{
            updateProductRequestDto = objectMapper.readValue(requestJson, UpdateProductVariantRequestDto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(adminProductService.updateProductVariant(updateProductRequestDto, images));
    }

    @PostMapping("/options")
    public ResponseEntity<List<AdminProductVariantDto>> createOption(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody CreateProductOptionDto createProductOptionDTO) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return ResponseEntity.ok(adminProductService.getAllUniqueOptions());
    }
}
