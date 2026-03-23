package com.bkeuty.product.dto.admin.branddto.updatebranddto;

import com.bkeuty.product.enums.BrandStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProductBrandRequestDto {
    private String brandName;
    private String image;
    private String description;
    private BrandStatus brandStatus;
}
