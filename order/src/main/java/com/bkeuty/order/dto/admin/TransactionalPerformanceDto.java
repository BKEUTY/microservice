package com.bkeuty.order.dto.admin;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionalPerformanceDto {
    private LocalDateTime date;
    private Integer entityId;
    private String entityName;
    private Integer productId;
    private String productVariantName;
    private Long quantity;
    private BigDecimal revenue;
    private BigDecimal profit;
 
    public TransactionalPerformanceDto(LocalDateTime date, Integer entityId, String entityName, Integer productId, String productVariantName, Long quantity, BigDecimal revenue) {
        this.date = date;
        this.entityId = entityId;
        this.entityName = entityName;
        this.productId = productId;
        this.productVariantName = productVariantName;
        this.quantity = quantity;
        this.revenue = revenue;
        this.profit = revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;
    }
}
