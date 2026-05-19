package com.bkeuty.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundOrderEventDto {
    private Integer refundOrderId;
    private Integer orderId;
    private String userId;
    private BigDecimal total;
    private String fromAddress;
    private String phoneNumber;
    private List<Integer> orderItemIds;
    private List<String> evidenceImageUrls;
    private String note;
}
