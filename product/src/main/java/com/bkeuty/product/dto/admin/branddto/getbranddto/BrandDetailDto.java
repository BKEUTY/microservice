package com.bkeuty.product.dto.admin.branddto.getbranddto;

import com.bkeuty.product.enums.BrandStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandDetailDto {
    private Integer id;
    private String name;
    private String description;
    private String image;
    private BrandStatus brandStatus;
}
