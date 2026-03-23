package com.bkeuty.order.entity;

import com.bkeuty.order.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private BigDecimal total;
    private String paymentMethod;
    private LocalDate orderDate;
    private String address;
    
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;
}
