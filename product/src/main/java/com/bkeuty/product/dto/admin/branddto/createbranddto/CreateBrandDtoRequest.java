package com.bkeuty.product.dto.admin.branddto.createbranddto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBrandDtoRequest {
    private String brandName;
    private String description;
    private String image;
}
