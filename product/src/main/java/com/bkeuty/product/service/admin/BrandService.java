package com.bkeuty.product.service.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoRequest;
import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoResponse;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDetailDto;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandRequestDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandResponseDto;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.repository.ProductBrandRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class BrandService {
    private final ProductBrandRepository productBrandRepository;
    public BrandService(ProductBrandRepository productBrandRepository) {
        this.productBrandRepository = productBrandRepository;
    }

    public CreateBrandDtoResponse createBrand(CreateBrandDtoRequest createBrandDtoRequest) {
        ProductBrand productBrand = new ProductBrand();
        productBrand.setBrandName(createBrandDtoRequest.getBrandName());
        productBrand.setDescription(createBrandDtoRequest.getDescription());
        productBrand.setImage(createBrandDtoRequest.getImage());
        return toCreateBrandDtoResponse(productBrandRepository.save(productBrand));
    }
    private CreateBrandDtoResponse toCreateBrandDtoResponse(ProductBrand productBrand) {
        return CreateBrandDtoResponse.builder()
                .id(productBrand.getId())
                .brandName(productBrand.getBrandName())
                .description(productBrand.getDescription())
                .image(productBrand.getImage())
                .brandStatus(productBrand.getBrandStatus())
                .build();
    }

    public UpdateProductBrandResponseDto updateBrand(Integer brandId, UpdateProductBrandRequestDto updateBrandDtoRequest) {
        ProductBrand productBrand = productBrandRepository.findById(brandId).orElseThrow(() -> new RuntimeException("brand not found"));
        if(updateBrandDtoRequest.getBrandName() != null){
            productBrand.setBrandName(updateBrandDtoRequest.getBrandName());
        }
        if(updateBrandDtoRequest.getDescription() != null){
            productBrand.setDescription(updateBrandDtoRequest.getDescription());
        }
        if(updateBrandDtoRequest.getImage() != null){
            productBrand.setImage(updateBrandDtoRequest.getImage());
        }
        if(updateBrandDtoRequest.getBrandStatus() != null){
            productBrand.setBrandStatus(updateBrandDtoRequest.getBrandStatus());
        }
        return toUpdateBrandResponseDto(productBrandRepository.save(productBrand));
    }
    private UpdateProductBrandResponseDto toUpdateBrandResponseDto(ProductBrand productBrand) {
        return UpdateProductBrandResponseDto.builder()
                .id(productBrand.getId())
                .brandName(productBrand.getBrandName())
                .description(productBrand.getDescription())
                .image(productBrand.getImage())
                .brandStatus(productBrand.getBrandStatus())
                .build();
    }
    public void deleteBrand(Integer brandId) {
        try {
            productBrandRepository.deleteById(brandId);
        } catch (Exception e) {
            throw new RuntimeException("delete brand fail id:"+brandId);
        }
    }
    public Page<BrandDto>  getBrands(String search, Pageable pageable) {
        Specification<ProductBrand> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String keyword = search.trim().toLowerCase();
                String searchTerm = "%" + keyword + "%";
                List<Predicate> searchPredicates = new ArrayList<>();
                try {
                    Integer id = Integer.parseInt(keyword);
                    searchPredicates.add(cb.equal(root.get("id"), id));
                } catch (NumberFormatException e) {}
                searchPredicates.add(cb.like(cb.lower(root.get("brandName")), searchTerm));
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productBrandRepository.findAll(spec, pageable).map(this::toBrandDto);
    }
    private BrandDto toBrandDto(ProductBrand productBrand) {
        return BrandDto.builder()
                .id(productBrand.getId())
                .brandStatus(productBrand.getBrandStatus())
                .description(productBrand.getDescription())
                .image(productBrand.getImage())
                .name(productBrand.getBrandName())
                .build();
    }
    public BrandDetailDto getBrandDetail(Integer brandId) {
        ProductBrand  productBrand = productBrandRepository.findById(brandId).orElseThrow(() -> new RuntimeException("brand not found"));
        return toBrandDetailDto(productBrand);
    }
    private BrandDetailDto toBrandDetailDto(ProductBrand productBrand) {
        return BrandDetailDto.builder()
                .id(productBrand.getId())
                .brandStatus(productBrand.getBrandStatus())
                .description(productBrand.getDescription())
                .image(productBrand.getImage())
                .name(productBrand.getBrandName())
                .build();
    }
}
