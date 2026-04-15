package com.bkeuty.order.service.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<AdminOrderDto> getAllOrders(Pageable pageable, String status, LocalDate startDate, LocalDate endDate) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                String trimmedStatus = status.trim();
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), 
                        PaymentStatus.valueOf(trimmedStatus.toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid order status: " + trimmedStatus + ". Allowed values: " + 
                        java.util.Arrays.toString(PaymentStatus.values()));
                }
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        if (orderPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<AdminOrderDto> adminOrderDtos = orderPage.getContent().stream()
                .map(this::toAdminOrderDto)
                .collect(Collectors.toList());

        return new PageImpl<>(adminOrderDtos, pageable, orderPage.getTotalElements());
    }

    public AdminOrderDto getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Order not found with ID: " + orderId));
        return toAdminOrderDto(order);
    }

    public AdminOrderDto updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Order not found with ID: " + orderId));

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be null or blank");
        }

        try {
            order.setStatus(PaymentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Invalid order status: " + status + ". Allowed values: " + 
                java.util.Arrays.toString(PaymentStatus.values()));
        }

        Order savedOrder = orderRepository.save(order);
        return toAdminOrderDto(savedOrder);
    }

    private AdminOrderDto toAdminOrderDto(Order order) {
        List<AddToCartResponseDto> itemDtos = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                    .map(item -> AddToCartResponseDto.builder()
                        .productVariantId(item.getProductVariantId())
                        .productVariantName(item.getProductVariantName())
                        .productVariantImage(item.getProductImageUrl())
                        .price(item.getPrice())
                        .promotionPrice(item.getPromotionPrice())
                        .quantity(item.getQuantity())
                        .build())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return AdminOrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userName(order.getUserName())
                .total(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO)
                .shippingFee(order.getShippingFee())
                .paymentMethod(order.getPaymentMethod())
                .orderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now())
                .address(order.getAddress())
                .status(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name())
                .items(itemDtos)
                .build();
    }
}
