package com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionRequest;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true // Keeps the "type" field accessible in the DTO
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateProductPromotionRequest.class, name = "PRODUCT")
})
public abstract class CreatePromotionRequest {
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String type;
    private DiscountType discountType;
    private PromotionStatus status;
    private Integer discountValue;
}
