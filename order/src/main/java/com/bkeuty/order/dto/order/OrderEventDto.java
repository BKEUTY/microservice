package com.bkeuty.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {
    private Integer orderId;
    private String userId;
    private Integer voucherId;
    private String status; // COMPLETED, FAILED
}
