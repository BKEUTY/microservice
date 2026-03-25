package com.bkeuty.order.service.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.dto.order.*;
import com.bkeuty.order.entity.CartItem;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.exception.CartItemNotFound;
import com.bkeuty.order.repository.CartItemRepository;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient productWebClient;
    
    @Value("${sepay.account-number:}")
    private String accountNumber;
    @Value("${sepay.bank:}")
    private String bank;
    @Value("${sepay.template:}")
    private String template;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository, WebClient productWebClient) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productWebClient = productWebClient;
    }

    public ResponseEntity<?> placeOrder(TokenValidationResponseDto userInfo, PlaceOrderRequestDto request) {
        List<OrderCartItemDto> orderItemList = request.getOrderItems();
        if (orderItemList == null || orderItemList.isEmpty()) {
            return ResponseEntity.badRequest().body("Order items cannot be empty");
        }

        Order order = Order.builder()
                .orderDate(LocalDate.now())
                .address(request.getAddress())
                .paymentMethod(request.getPaymentMethod())
                .userId(userInfo.getUserId())
                .status(PaymentStatus.UNPAID)
                .build();

        Order orderSave = orderRepository.save(order);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemDto> decreaseVariants = new ArrayList<>();
        List<AddToCartResponseDto> items = new ArrayList<>();

        for (OrderCartItemDto orderCartItemDto : orderItemList) {
            CartItem cartItems = cartItemRepository.findById(orderCartItemDto.getCartItemId())
                    .orElseThrow(() -> new CartItemNotFound("Cart item not found", orderCartItemDto.getCartItemId()));
            
            decreaseVariants.add(new OrderItemDto(cartItems.getProductVariant(), cartItems.getQuantity()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(orderSave);
            orderItem.setProductVariantId(cartItems.getProductVariant());
            orderItem.setQuantity(cartItems.getQuantity());
            
            cartItemRepository.delete(cartItems);
            orderItemRepository.save(orderItem);
        }

        try {
            List<DecreaseStockResponseDto> decreaseStockResponseDtos = productWebClient.post()
                    .uri("/api/inventory/internal/decreaseStock")
                    .bodyValue(new DecreaseStockRequestDto(decreaseVariants))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<DecreaseStockResponseDto>>() {})
                    .block();

            if (decreaseStockResponseDtos != null) {
                for (DecreaseStockResponseDto dto : decreaseStockResponseDtos) {
                    AddToCartResponseDto addToCartResponseDTO = AddToCartResponseDto.builder()
                            .price(dto.getPrice())
                            .productVariantId(dto.getProductVariantId())
                            .productVariantName(dto.getProductVariantName())
                            .quantity(dto.getQuantity())
                            .productVariantImage(dto.getProductVariantImage())
                            .promotionPrice(dto.getPromotionPrice())
                            .build();

                    if (dto.getPrice() != null && dto.getQuantity() != null) {
                        totalAmount = totalAmount.add(dto.getPromotionPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
                    }
                    items.add(addToCartResponseDTO);
                }
            }
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to communicate with inventory service: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Internal error processing stock: " + e.getMessage());
        }

        orderSave.setTotal(totalAmount);
        orderRepository.save(orderSave);
        
        OrderResponseDto placeOrderResponseDTO = new OrderResponseDto();
        placeOrderResponseDTO.setOrderId(orderSave.getId().toString());
        placeOrderResponseDTO.setOrderDate(LocalDate.now());
        placeOrderResponseDTO.setAddress(request.getAddress());
        placeOrderResponseDTO.setPaymentMethod(request.getPaymentMethod());
        placeOrderResponseDTO.setTotal(totalAmount);
        placeOrderResponseDTO.setItems(items);
        placeOrderResponseDTO.setQrCodeLink(generateQrCode(totalAmount, orderSave.getId()));
        
        return ResponseEntity.ok(placeOrderResponseDTO);
    }

    private String generateQrCode(BigDecimal total, Integer orderId) {
        return "https://qr.sepay.vn/img?acc=" + accountNumber + "&bank=" + bank + "&amount=" + total + "&des=DH" + orderId + "&template=" + template + "&download=false";
    }

    public ResponseEntity<List<OrderResponseDto>> getListOrders(String userId) {
        List<Order> listOrders = orderRepository.findByUserId(userId);
        List<OrderResponseDto> orderResponseDTOList = new ArrayList<>();
        for (Order orders : listOrders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(orders.getId());
            orderResponseDTOList.add(toOrderResponseDto(orders, items));
        }
        return ResponseEntity.ok(orderResponseDTOList);
    }

    public OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items) {
        OrderResponseDto orderResponseDTO = new OrderResponseDto();
        orderResponseDTO.setOrderId(order.getId() != null ? order.getId().toString() : "");
        orderResponseDTO.setOrderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now());
        orderResponseDTO.setAddress(order.getAddress());
        orderResponseDTO.setPaymentMethod(order.getPaymentMethod());
        orderResponseDTO.setTotal(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        orderResponseDTO.setItems(getAddToCartResponseDTOS(items));
        return orderResponseDTO;
    }

    private List<AddToCartResponseDto> getAddToCartResponseDTOS(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return new ArrayList<>();

        List<AddToCartResponseDto> itemList = new ArrayList<>();
        List<Integer> itemIds = items.stream().map(OrderItem::getProductVariantId).toList();
        
        try {
            Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                    .uri("/api/product/internal/variants/batch")
                    .bodyValue(itemIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                    .block();

            for (OrderItem orderItems : items) {
                AddToCartResponseDto addToCartResponseDTO = new AddToCartResponseDto();
                addToCartResponseDTO.setProductVariantId(orderItems.getProductVariantId());
                addToCartResponseDTO.setQuantity(orderItems.getQuantity());


                if (productVariants != null && productVariants.containsKey(orderItems.getProductVariantId())) {
                    ProductVariantDto productVariant = productVariants.get(orderItems.getProductVariantId());
                    addToCartResponseDTO.setProductVariantName(productVariant.getProductVariantName());
                    addToCartResponseDTO.setProductVariantImage(productVariant.getProductImageUrl());
                    addToCartResponseDTO.setPrice(productVariant.getPrice());
                    addToCartResponseDTO.setPromotionPrice(productVariant.getPromotionPrice());
                }
                itemList.add(addToCartResponseDTO);
            }
        } catch (Exception e) {
        }
        
        return itemList;
    }
}
