package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    public Page<AdminOrderDto> getAllOrders(Pageable pageable, String status, String search, LocalDate startDate, LocalDate endDate, String token) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                String trimmed = status.trim().toUpperCase(Locale.ROOT);
                try {
                    predicates.add(cb.equal(root.get("status"), OrderStatus.valueOf(trimmed)));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid order status: " + trimmed + ". Allowed: " + Arrays.toString(OrderStatus.values()));
                }
            }
            if (search != null && !search.isBlank()) {
                String keyword = search.trim();
                List<Predicate> searchPredicates = new ArrayList<>();

                if (keyword.matches("^\\d+$")) {
                    try {
                        Integer id = Integer.parseInt(keyword);
                        searchPredicates.add(cb.equal(root.get("id"), id));
                    } catch (NumberFormatException ignored) {}
                } else {
                    String likePattern = "%" + keyword.toLowerCase() + "%";
                    searchPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("userName"), "")), likePattern));
                    searchPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("address"), "")), likePattern));
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), endDate.atTime(23, 59, 59, 999999999)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        if (orderPage.isEmpty()) return Page.empty(pageable);

        List<AdminOrderDto> dtos = orderPage.getContent().stream()
                .map(o -> toAdminOrderDto(o, token))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, orderPage.getTotalElements());
    }

    public AdminOrderDto getOrderById(Integer orderId, String token) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        return toAdminOrderDto(order, token);
    }

    public AdminOrderDto updateOrderStatus(Integer orderId, String status, String token) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be blank");
        }
        try {
            order.setStatus(OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid order status: " + status + ". Allowed: " + Arrays.toString(OrderStatus.values()));
        }

        return toAdminOrderDto(orderRepository.save(order), token);
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
                                    .price(item.getProductVariantPrice())
                                    .promotionPrice(item.getPromotionPrice())
                                    .quantity(item.getQuantity())
                                    .build();
                        } else {
                            if (item.getProductVariantId() != null) missingVariantIds.add(item.getProductVariantId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!missingVariantIds.isEmpty()) {
                Map<Integer, ProductVariantDto> variants = fetchVariantMap(new ArrayList<>(missingVariantIds), token);
                order.getOrderItems().stream()
                        .filter(item -> item.getProductVariantName() == null || item.getProductVariantName().isBlank())
                        .map(item -> {
                            ProductVariantDto dto = variants.get(item.getProductVariantId());
                            if (dto == null) return null;
                            return AddToCartResponseDto.builder()
                                    .productVariantId(dto.getId())
                                    .productVariantName(dto.getProductVariantName())
                                    .productVariantImage(dto.getProductImageUrl())
                                    .price(dto.getPrice())
                                    .promotionPrice(dto.getPromotionPrice())
                                    .quantity(item.getQuantity())
                                    .build();
                        })
                        .filter(Objects::nonNull)
                        .forEach(itemDtos::add);
            }
        }

        return AdminOrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userName(order.getUserName())
                .total(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO)
                .shippingFee(order.getShippingFee())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .shippingStatus(order.getShippingStatus())
                .orderDate(order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : LocalDate.now())
                .address(order.getAddress())
                .status(order.getStatus() != null ? order.getStatus().name() : OrderStatus.NOT_CONFIRMED.name())
                .items(itemDtos)
                .availableStatuses(Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.toList()))
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
            log.error("Failed to fetch product variants for IDs: {}", variantIds, e);
            return Collections.emptyMap();
        }
    }
}
