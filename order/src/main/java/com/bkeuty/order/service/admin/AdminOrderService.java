package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient productWebClient;

    public AdminOrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, WebClient productWebClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productWebClient = productWebClient;
    }

    public Page<AdminOrderDto> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        if (orderPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Integer> orderIds = orderPage.stream().map(Order::getId).toList();
        List<OrderItem> allOrderItems = orderItemRepository.findByOrderIdIn(orderIds);
        List<Integer> variantIds = allOrderItems.stream().map(OrderItem::getProductVariantId).distinct().toList();

        Map<Integer, ProductVariantDto> productVariants = fetchVariantMap(variantIds);

        return orderPage.map(order -> {
            List<OrderItem> itemsForOrder = allOrderItems.stream()
                    .filter(item -> item.getOrder().getId().equals(order.getId()))
                    .toList();
            return toAdminOrderDto(order, itemsForOrder, productVariants);
        });
    }

    public AdminOrderDto getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<Integer> variantIds = items.stream().map(OrderItem::getProductVariantId).distinct().toList();
        Map<Integer, ProductVariantDto> productVariants = fetchVariantMap(variantIds);

        return toAdminOrderDto(order, items, productVariants);
    }

    public AdminOrderDto updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(PaymentStatus.valueOf(status.toUpperCase()));
        Order savedOrder = orderRepository.save(order);

        AdminOrderDto dto = new AdminOrderDto();
        dto.setId(savedOrder.getId());
        dto.setUserId(savedOrder.getUserId());
        dto.setTotal(savedOrder.getTotal());
        dto.setOrderDate(savedOrder.getOrderDate());
        dto.setStatus(savedOrder.getStatus() != null ? savedOrder.getStatus().name() : PaymentStatus.UNPAID.name());

        return dto;
    }

    private Map<Integer, ProductVariantDto> fetchVariantMap(List<Integer> variantIds) {
        try {
            return productWebClient.post()
                    .uri("/api/product/internal/variants/batch")
                    .bodyValue(variantIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                    .block();
        } catch (Exception e) {
            return null;
        }
    }

    private AdminOrderDto toAdminOrderDto(Order order, List<OrderItem> items, Map<Integer, ProductVariantDto> productVariants) {
        List<AddToCartResponseDto> itemDtos = new ArrayList<>();
        for (OrderItem item : items) {
            AddToCartResponseDto dto = new AddToCartResponseDto();
            dto.setProductVariantId(item.getProductVariantId());
            dto.setQuantity(item.getQuantity());

            if (productVariants != null && productVariants.containsKey(item.getProductVariantId())) {
                ProductVariantDto variant = productVariants.get(item.getProductVariantId());
                dto.setProductVariantName(variant.getProductVariantName());
                dto.setProductVariantImage(variant.getProductImageUrl());
                dto.setPrice(variant.getPrice());
                dto.setPromotionPrice(variant.getPromotionPrice());
            }
            itemDtos.add(dto);
        }

        AdminOrderDto adminOrderDto = new AdminOrderDto();
        adminOrderDto.setId(order.getId());
        adminOrderDto.setUserId(order.getUserId());
        adminOrderDto.setTotal(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        adminOrderDto.setPaymentMethod(order.getPaymentMethod().toString());
        adminOrderDto.setOrderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now());
        adminOrderDto.setAddress(order.getAddress());
        adminOrderDto.setStatus(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name());
        adminOrderDto.setItems(itemDtos);

        return adminOrderDto;
    }
}
