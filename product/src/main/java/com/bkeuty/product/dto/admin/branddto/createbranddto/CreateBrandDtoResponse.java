package com.bkeuty.product.dto.admin.branddto.createbranddto;

import com.bkeuty.product.enums.BrandStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBrandDtoResponse {
    private Integer id;
    private String brandName;
    private String description;
    private String image;
    private BrandStatus brandStatus;
}
