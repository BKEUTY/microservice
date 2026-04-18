package com.bkeuty.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWebhookData {
    private Integer id;
    private String gateway;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType;
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String subAccount;
    private String referenceCode;
    private String description;
}
