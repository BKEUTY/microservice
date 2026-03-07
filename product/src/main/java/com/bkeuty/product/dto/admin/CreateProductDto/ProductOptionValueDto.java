package com.bkeuty.product.dto.admin.CreateProductDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductOptionValueDto {
    private String optionName;
    private List<String> optionValues;
}
