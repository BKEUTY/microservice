package com.bkeuty.order.entity;

import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentMethod;
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
    private PaymentMethod paymentMethod;
    private LocalDate orderDate;
    private String address;
    private BigDecimal shippingFee;
    private String estimatedShippingDate;
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    private String buyerName;
    private String buyerNumber;
    private String buyerNote;
    private String shippingCode;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Builder.Default
    private String shippingStatus = "NOT_CREATED";
    @Builder.Default
    private OrderStatus status = OrderStatus.NOT_CONFIRMED;
}
