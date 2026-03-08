package com.bkeuty.product.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductDto {
    private Integer productId;
    private String name;
    private String image;
    private List<String> categories;
    private String description;
}
