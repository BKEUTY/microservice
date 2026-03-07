package com.bkeuty.product.controller.admin;

import com.bkeuty.product.dto.admin.AdminProductVariantDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductOptionDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductRequestDto;
import com.bkeuty.product.dto.admin.CreateProductDto.CreateProductResponseDto;
import com.bkeuty.product.dto.admin.UpdateProductDto.UpdateProductRequestDto;
import com.bkeuty.product.dto.admin.UpdateProductVariantDto.UpdateProductVariantRequestDto;
import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.AdminProductService;
import com.bkeuty.product.service.authservice.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/product")
public class ProductController {
    private final AuthService authService;
    private final AdminProductService adminProductService;
    public ProductController(AdminProductService adminProductService, AuthService authService) {
        this.adminProductService = adminProductService;
        this.authService = authService;
    }


    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<AdminProductVariantDto>> getAllProductVariants(@RequestHeader("Authorization") String bearerToken,
            @PathVariable Integer productId
    ) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId()==null||tokenValidationResponseDto.getUserRole()==null||!"admin".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.getAllProductVariants(productId));
    }
    @PostMapping()
    public ResponseEntity createProduct(@RequestHeader("Authorization") String bearerToken, @Valid @RequestBody CreateProductRequestDto createProductRequestDTO) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId()==null||tokenValidationResponseDto.getUserRole()==null||!"admin".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CreateProductResponseDto savedProduct = adminProductService.createProduct(createProductRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
    @PutMapping()
    public ResponseEntity updateProduct(@RequestHeader("Authorization") String bearerToken, @Valid @RequestBody UpdateProductRequestDto updateProductRequestDTO) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId()==null||tokenValidationResponseDto.getUserRole()==null||!"admin".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.updateProduct(updateProductRequestDTO));
    }
    @PutMapping("/variants")
    public ResponseEntity updateProductVariant(@RequestHeader("Authorization") String bearerToken, @Valid @RequestBody UpdateProductVariantRequestDto updateProductVariantRequestDTO) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId()==null||tokenValidationResponseDto.getUserRole()==null||!"admin".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK).body(adminProductService.updateProductVariant(updateProductVariantRequestDTO));
    }
    @PostMapping("/options")
    public ResponseEntity createOption(@RequestHeader("Authorization") String bearerToken, @Valid @RequestBody CreateProductOptionDto createProductOptionDTO) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId()==null||tokenValidationResponseDto.getUserRole()==null||!"admin".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(adminProductService.createOptionValue(createProductOptionDTO));
    }
}
