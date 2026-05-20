package com.bkeuty.product.dto.admin.UpdateProductDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProductResponseDto {
    @NotBlank
    @NotNull
    private Integer id;
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    private String description ;
    private List<String> productCategories ;
    private List<String> image;
}
