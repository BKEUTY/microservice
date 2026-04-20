package com.bkeuty.order.dto.admin;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantMappingDto {
    private Integer id;
    private String variantName;
    private Integer brandId;
    private String brandName;
    private Integer categoryId;
    private String categoryName;
}
