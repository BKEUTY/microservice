package com.bkeuty.user_service.dto.refund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event consumed from {@code refund-wallet-success-topic}.
 * Published by User Service after it successfully credits the user's wallet.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundWalletSuccessEventDto {
    private Integer refundOrderId;
    private String userId;
}
