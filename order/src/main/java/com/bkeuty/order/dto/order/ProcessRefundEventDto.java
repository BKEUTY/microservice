package com.bkeuty.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event published to {@code process-refund-topic}.
 * User Service listens to this and credits the user's wallet.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessRefundEventDto {
    private Integer refundOrderId;
    private Integer orderId;
    private String userId;
    private BigDecimal amount;
}
