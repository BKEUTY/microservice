package com.bkeuty.order.service.order;

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
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
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

        List<Integer> variantIds = allOrderItems.stream()
                .map(OrderItem::getProductVariantId)
                .distinct()
                .toList();

        Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                .uri("/api/product/internal/variants/batch")
                .bodyValue(variantIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                .block();

        return orderPage.map(order -> {
            List<OrderItem> itemsForOrder = allOrderItems.stream()
                    .filter(item -> item.getOrder().getId().equals(order.getId()))
                    .toList();

            List<AddToCartResponseDto> itemDtos = itemsForOrder.stream().map(item -> {
                ProductVariantDto variant = productVariants != null ? productVariants.get(item.getProductVariantId()) : null;
                AddToCartResponseDto dto = new AddToCartResponseDto();
                dto.setProductVariantId(item.getProductVariantId());
                dto.setQuantity(item.getQuantity());
                
                if (variant != null) {
                    dto.setProductVariantName(variant.getProductVariantName());
                    dto.setProductVariantImage(variant.getProductImageUrl());
                    dto.setPrice(variant.getPrice());
                }
                return dto;
            }).toList();

            AdminOrderDto adminOrderDto = new AdminOrderDto();
            adminOrderDto.setId(order.getId());
            adminOrderDto.setUserId(order.getUserId());
            adminOrderDto.setTotal(order.getTotal());
            adminOrderDto.setPaymentMethod(order.getPaymentMethod());
            adminOrderDto.setOrderDate(order.getOrderDate());
            adminOrderDto.setAddress(order.getAddress());
            adminOrderDto.setStatus(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name());
            adminOrderDto.setItems(itemDtos);
            
            return adminOrderDto;
        });
    }

    public AdminOrderDto updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        order.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        Order savedOrder = orderRepository.save(order);

        AdminOrderDto dto = new AdminOrderDto();
        dto.setId(savedOrder.getId());
        dto.setStatus(savedOrder.getStatus() != null ? savedOrder.getStatus().name() : PaymentStatus.UNPAID.name());
        dto.setUserId(savedOrder.getUserId());
        dto.setTotal(savedOrder.getTotal());
        
        return dto;
    }

    public AdminOrderDto getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> itemsForOrder = orderItemRepository.findByOrderId(order.getId());
        List<Integer> variantIds = itemsForOrder.stream()
                .map(OrderItem::getProductVariantId)
                .distinct()
                .toList();

        Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                .uri("/api/product/internal/variants/batch")
                .bodyValue(variantIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                .block();

        List<AddToCartResponseDto> itemDtos = itemsForOrder.stream().map(item -> {
            ProductVariantDto variant = productVariants != null ? productVariants.get(item.getProductVariantId()) : null;
            AddToCartResponseDto dto = new AddToCartResponseDto();
            dto.setProductVariantId(item.getProductVariantId());
            dto.setQuantity(item.getQuantity());
            
            if (variant != null) {
                dto.setProductVariantName(variant.getProductVariantName());
                dto.setProductVariantImage(variant.getProductImageUrl());
                dto.setPrice(variant.getPrice());
            }
            return dto;
        }).toList();

        AdminOrderDto adminOrderDto = new AdminOrderDto();
        adminOrderDto.setId(order.getId());
        adminOrderDto.setUserId(order.getUserId());
        adminOrderDto.setTotal(order.getTotal());
        adminOrderDto.setPaymentMethod(order.getPaymentMethod());
        adminOrderDto.setOrderDate(order.getOrderDate());
        adminOrderDto.setAddress(order.getAddress());
        adminOrderDto.setStatus(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name());
        adminOrderDto.setItems(itemDtos);
        
        return adminOrderDto;
    }
}