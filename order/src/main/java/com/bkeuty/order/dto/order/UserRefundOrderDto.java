package com.bkeuty.order.dto.order;

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
public class UserRefundOrderDto {
    private Integer id;
    private Integer orderId;
    private BigDecimal total;
    private String fromAddress;
    private String phoneNumber;
    private String note;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private List<String> evidenceImageUrls;
    private List<String> items;
}
