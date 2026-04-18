package com.bkeuty.payment_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String gateway;

    private LocalDateTime transactionDate;

    private String accountNumber;

    private String subAccount;

    private BigDecimal amountIn = BigDecimal.ZERO;

    private BigDecimal amountOut = BigDecimal.ZERO;
    private BigDecimal accumulated = BigDecimal.ZERO;
    private String code;
    private String transactionContent;
    private String referenceNumber;
    private String body;
    private LocalDateTime createdAt;

}
