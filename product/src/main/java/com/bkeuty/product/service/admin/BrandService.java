package com.bkeuty.product.service.admin;

import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoRequest;
import com.bkeuty.product.dto.admin.branddto.createbranddto.CreateBrandDtoResponse;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDetailDto;
import com.bkeuty.product.dto.admin.branddto.getbranddto.BrandDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandRequestDto;
import com.bkeuty.product.dto.admin.branddto.updatebranddto.UpdateProductBrandResponseDto;
import com.bkeuty.product.entity.ProductBrand;
import com.bkeuty.product.repository.ProductBrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Page<BrandDto>  getBrands(Pageable pageable) {
        return productBrandRepository.findAll(pageable).map(this::toBrandDto);
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
