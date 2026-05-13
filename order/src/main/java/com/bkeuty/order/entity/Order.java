package com.bkeuty.order.entity;

import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.bkeuty.order.enums.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @Column(name = "order_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime orderDate;
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

    @Column(name = "user_name")
    private String userName;

    @Builder.Default
    private String shippingStatus = "NOT_CREATED";
    @Builder.Default
    private OrderStatus status = OrderStatus.NOT_CONFIRMED;
    
    @Column(name = "voucher_id")
    private Integer voucherId;

    @Column(name = "voucher_discount_amount")
    private BigDecimal voucherDiscountAmount;

    @Column(name = "membership_level")
    private Integer membershipLevel;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems;
}
