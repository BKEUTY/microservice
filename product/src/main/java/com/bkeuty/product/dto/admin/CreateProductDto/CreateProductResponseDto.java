package com.bkeuty.product.dto.admin.CreateProductDto;

import com.bkeuty.product.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductResponseDto {
    private Integer id;
    private String name;
    private String description = "";
    private List<String> categories = new ArrayList<>();
    private List<ProductImage> image;
    private String brandName = "";
}
