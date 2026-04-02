package com.bkeuty.order.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOrderDeliveredRequestDto {
    private String userId;
    private Integer variantId;
}
