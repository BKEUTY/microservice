package com.bkeuty.product.controller.admin;

import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoRequest;
import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoResponse;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandRequestDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandResponseDto;
import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.admin.BrandService;
import com.bkeuty.product.service.authservice.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/brand")
public class BrandController {
    private final BrandService brandService;
    private final AuthService authService;

    public BrandController(BrandService brandService, AuthService authService) {
        this.brandService = brandService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<Page<BrandDto>> getAllBrands(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {
            
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        return ResponseEntity.ok(brandService.getBrands(pageable));
    }
    @PostMapping
    public ResponseEntity<CreateBrandDtoResponse> addBrand(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody CreateBrandDtoRequest request) {
            
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(request));
    }

    @PutMapping("/{brandId}")
    public ResponseEntity<UpdateProductBrandResponseDto> updateBrand(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer brandId, 
            @RequestBody UpdateProductBrandRequestDto requestDto) {
            
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.OK).body(brandService.updateBrand(brandId, requestDto));
    }
    @DeleteMapping("/{brandId}")
    public ResponseEntity<?> deleteBrand(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer brandId) {
            
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        brandService.deleteBrand(brandId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
