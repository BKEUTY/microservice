package com.bkeuty.order.dto.admin;

import com.bkeuty.order.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminRefundOrderDto {
    private Integer refundOrderId;
    private Integer orderId;
    private String userId;
    private String userName;
    private BigDecimal total;
    private RefundStatus status;
    private String fromAddress;
    private String phoneNumber;
    private String note;
    private LocalDateTime createdAt;
    private List<String> evidenceImageUrls;
    private List<AdminRefundItemDto> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminRefundItemDto {
        private Integer orderItemId;
        private Integer productVariantId;
        private String productVariantName;
        private String productImageUrl;
        private Integer quantity;
        private BigDecimal unitRefundAmount;
    }
}
