package com.bkeuty.product.dto.user.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DecreaseStockStatusDto {
    Boolean isSuccess;
    Integer orderId;
    List<DecreaseStockResponseDto> failDecreaseStockItems;
}
