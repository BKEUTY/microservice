package com.bkeuty.product.dto.admin.CreateProductDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductOptionDto {
    private Integer productId;
    @NotEmpty
    List<ProductOptionValueDto> productOptionValues;
}
