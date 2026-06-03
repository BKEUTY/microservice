package com.bkeuty.product.controller.admin;

import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.authservice.AuthService;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.repository.ProductCategoryRepository;
import com.bkeuty.product.util.ProductSortUtils;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/admin/category")
public class AdminCategoryController {
    private final ProductCategoryRepository categoryRepository;
    private final AuthService authService;
    public AdminCategoryController(ProductCategoryRepository categoryRepository, AuthService authService) {
        this.categoryRepository = categoryRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<ProductCategory>> getAllCategories(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sort", required = false) String[] sort) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        
        Sort sortObj = ProductSortUtils.parseCategorySort(sort, "id");
        
        if (search != null && !search.isBlank()) {
            String keyword = search.trim().toLowerCase();
            String searchTerm = "%" + keyword + "%";
            Specification<ProductCategory> spec = (root, query, cb) -> {
                List<Predicate> searchPredicates = new ArrayList<>();
                try {
                    Integer id = Integer.parseInt(keyword);
                    searchPredicates.add(cb.equal(root.get("id"), id));
                } catch (NumberFormatException e) {}
                searchPredicates.add(cb.like(cb.lower(root.get("categoryName")), searchTerm));
                return cb.or(searchPredicates.toArray(new Predicate[0]));
            };
            return ResponseEntity.ok(categoryRepository.findAll(spec, sortObj));
        }
        
        return ResponseEntity.ok(categoryRepository.findAll(sortObj));
    }

    @PostMapping
    public ResponseEntity<ProductCategory> createCategory(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody ProductCategory category) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> updateCategory(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "id") Integer id, 
            @RequestBody ProductCategory categoryDetails) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return categoryRepository.findById(id).map(category -> {
            category.setCategoryName(categoryDetails.getCategoryName());
            return ResponseEntity.ok(categoryRepository.save(category));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "id") Integer id) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return categoryRepository.findById(id).map(category -> {
            categoryRepository.delete(category);
            return ResponseEntity.ok().<Void>build();
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }
}
