package com.bkeuty.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetailDto {
    @JsonProperty("id")
    private Integer productId;

    @JsonProperty("name")
    private String variantName;

    private BigDecimal originPrice;

    @JsonProperty("promotionPrice")
    private BigDecimal discountPrice;

    @JsonProperty("image")
    private String imageUrl;

    private Integer stockQuantity;
    private Integer sold;
    private String brand;
    private List<CategoryDetailDto> categories;
    private String status;
    private String description;
    private Double averageRating;
    private Integer reviewCount;
    private String appliedPromotionType;
}
