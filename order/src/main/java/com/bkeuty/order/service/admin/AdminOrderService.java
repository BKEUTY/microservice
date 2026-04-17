package com.bkeuty.order.service.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
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
    private final WebClient productWebClient;

    public AdminOrderService(OrderRepository orderRepository, WebClient productWebClient) {
        this.orderRepository = orderRepository;
        this.productWebClient = productWebClient;
    }

    public Page<AdminOrderDto> getAllOrders(Pageable pageable, String status, LocalDate startDate, LocalDate endDate, String token) {
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
                .map(o -> this.toAdminOrderDto(o, token))
                .collect(Collectors.toList());

        return new PageImpl<>(adminOrderDtos, pageable, orderPage.getTotalElements());
    }

    public AdminOrderDto getOrderById(Integer orderId, String token) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Order not found with ID: " + orderId));
        return toAdminOrderDto(order, token);
    }

    public AdminOrderDto updateOrderStatus(Integer orderId, String status, String token) {
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
        return toAdminOrderDto(savedOrder, token);
    }

    private AdminOrderDto toAdminOrderDto(Order order, String token) {
        List<AddToCartResponseDto> itemDtos = new ArrayList<>();
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            Set<Integer> missingVariantIds = new HashSet<>();
            
            itemDtos = order.getOrderItems().stream()
                    .map(item -> {
                        if (item.getProductVariantName() != null && !item.getProductVariantName().isBlank()) {
                            return AddToCartResponseDto.builder()
                                    .productVariantId(item.getProductVariantId())
                                    .productVariantName(item.getProductVariantName())
                                    .productVariantImage(item.getProductImageUrl())
                                    .price(item.getPrice())
                                    .promotionPrice(item.getPromotionPrice())
                                    .quantity(item.getQuantity())
                                    .build();
                        } else {
                            if (item.getProductVariantId() != null) {
                                missingVariantIds.add(item.getProductVariantId());
                            }
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!missingVariantIds.isEmpty()) {
                Map<Integer, ProductVariantDto> variants = fetchVariantMap(new ArrayList<>(missingVariantIds), token);
                
                List<AddToCartResponseDto> fallbackItems = order.getOrderItems().stream()
                        .filter(item -> item.getProductVariantName() == null || item.getProductVariantName().isBlank())
                        .map(item -> {
                            ProductVariantDto variantDto = variants.get(item.getProductVariantId());
                            if (variantDto != null) {
                                return AddToCartResponseDto.builder()
                                        .productVariantId(variantDto.getId())
                                        .productVariantName(variantDto.getProductVariantName())
                                        .productVariantImage(variantDto.getProductImageUrl())
                                        .price(variantDto.getPrice())
                                        .promotionPrice(variantDto.getPromotionPrice())
                                        .quantity(item.getQuantity())
                                        .build();
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                
                itemDtos.addAll(fallbackItems);
            }
        }

        return AdminOrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userName(order.getUserName())
                .total(emptyIfNull(order.getTotal(), BigDecimal.ZERO))
                .shippingFee(order.getShippingFee())
                .paymentMethod(order.getPaymentMethod())
                .orderDate(order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : LocalDate.now())
                .address(order.getAddress())
                .status(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name())
                .items(itemDtos)
                .build();
    }

    private Map<Integer, ProductVariantDto> fetchVariantMap(List<Integer> variantIds, String token) {
        if (variantIds == null || variantIds.isEmpty()) return Collections.emptyMap();
        try {
            Map<Integer, ProductVariantDto> result = productWebClient.post()
                    .uri("/api/product/internal/variants/batch")
                    .bodyValue(variantIds)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                    .block();
            return result != null ? result : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Failed to fetch product variants from product-service for IDs: {}", variantIds, e);
            return Collections.emptyMap();
        }
    }

    private <T> T emptyIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
