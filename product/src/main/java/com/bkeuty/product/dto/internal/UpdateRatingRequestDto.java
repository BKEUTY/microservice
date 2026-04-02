package com.bkeuty.product.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRatingRequestDto {
    private Integer variantId;
    private Double averageRating;
    private Integer reviewCount;
}
