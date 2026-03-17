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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient productWebClient;
    @Value("${sepay.account-number}")
    private String accountNumber;
    @Value("${sepay.bank}")
    private String bank;
    @Value("${sepay.template}")
    private String template;
    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository, WebClient productWebClient) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productWebClient = productWebClient;
    }
    public ResponseEntity<OrderResponseDto> placeOrder (TokenValidationResponseDto userInfo, PlaceOrderRequestDto request){
        List<OrderCartItemDto> orderItemList = request.getOrderItems();
//        Users orderUser = usersRepository.findById(request.getUserId()).orElseThrow(()->new UserNotFoundException("User not found"));
        Order order = Order.builder()
                .orderDate(LocalDate.now())
                .address(request.getAddress())
                .paymentMethod(request.getPaymentMethod()).userId(userInfo.getUserId())
                .status(PaymentStatus.UNPAID)
                .build();

        Order orderSave = orderRepository.save(order);
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        List<OrderItemDto> decreaseVariants = new ArrayList<>();
//        List<ProductVariantDto> productVariantDtos = productWebClient.post().uri("/api/internal/inventory/decreaseStock")
//                .bodyValue()
        List<AddToCartResponseDto> items = new ArrayList<>();
        for(OrderCartItemDto orderCartItemDto : orderItemList){
            OrderItem orderItem = new OrderItem();
            CartItem cartItems = cartItemRepository.findById(orderCartItemDto.getCartItemId()).orElseThrow(()->new CartItemNotFound("Cart item not found", orderCartItemDto.getCartItemId()));
//            ProductVariant productVariant = cartItems.getProductVariant();
            decreaseVariants.add(new OrderItemDto(cartItems.getProductVariant(),cartItems.getQuantity()));
            orderItem.setOrder(orderSave);
            orderItem.setProductVariantId(cartItems.getProductVariant());
            orderItem.setQuantity(cartItems.getQuantity());
//            totalAmount = totalAmount.add(
//                    productVariant.getPrice().multiply(BigDecimal.valueOf(cartItems.getQuantity()))
//            );
//            Integer currentStock = productVariant.getStockQuantity();
//            productVariant.setStockQuantity(currentStock - cartItems.getQuantity());
//            productVariantsRepository.save(productVariant);
//            AddToCartResponseDto addToCartResponseDTO = AddToCartResponseDto.builder()
//                    .price(productVariant.getPrice())
//                    .productVariantId(productVariant.getId())
//                    .productVariantName(productVariant.getProductVariantName())
//                    .quantity(cartItems.getQuantity())
//                    .build();
//            items.add(addToCartResponseDTO);
            cartItemRepository.delete(cartItems);
            orderItemRepository.save(orderItem);
        }
        System.out.println("Number of order items: " + decreaseVariants.size());
        List<DecreaseStockResponseDto> decreaseStockResponseDtos = productWebClient.post().uri("/api/inventory/internal/decreaseStock").bodyValue(new DecreaseStockRequestDto(decreaseVariants))
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<DecreaseStockResponseDto>>(){}).block();
        for(DecreaseStockResponseDto decreaseStockResponseDto : decreaseStockResponseDtos){
            AddToCartResponseDto addToCartResponseDTO = AddToCartResponseDto.builder()
                    .price(decreaseStockResponseDto.getPrice())
                    .productVariantId(decreaseStockResponseDto.getProductVariantId())
                    .productVariantName(decreaseStockResponseDto.getProductVariantName())
                    .quantity(decreaseStockResponseDto.getQuantity())
                    .productVariantImage(decreaseStockResponseDto.getProductVariantImage())
                    .build();
            totalAmount = totalAmount.add(
                    addToCartResponseDTO.getPrice().multiply(BigDecimal.valueOf(addToCartResponseDTO.getQuantity()))
            );
            items.add(addToCartResponseDTO);
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
        placeOrderResponseDTO.setQrCodeLink(generateQrCode(totalAmount,orderSave.getId()));
        return ResponseEntity.ok(placeOrderResponseDTO);
    }
    private String generateQrCode(BigDecimal total, Integer orderId){
        return "https://qr.sepay.vn/img?acc="+accountNumber+"&bank="+bank+"&amount="+total+"&des=DH"+orderId+"&template="+template+"&download=false";
    }
    public ResponseEntity<List<OrderResponseDto>> getListOrders(String userId){
        List<Order> listOrders = orderRepository.findByUserId(userId);
        List<OrderResponseDto> orderResponseDTOList = new ArrayList<>();
        for(Order orders : listOrders){
            List<OrderItem> items = orderItemRepository.findByOrderId(orders.getId());
            orderResponseDTOList.add(toOrderResponseDto(orders, items));
        }
        return ResponseEntity.ok(orderResponseDTOList);
    }
    public OrderResponseDto toOrderResponseDto (Order order, List<OrderItem> items){
        OrderResponseDto orderResponseDTO = new OrderResponseDto();
        orderResponseDTO.setOrderDate(LocalDate.now());
        orderResponseDTO.setAddress(order.getAddress());
        orderResponseDTO.setPaymentMethod(order.getPaymentMethod());
        orderResponseDTO.setTotal(order.getTotal());
        List<AddToCartResponseDto> itemList = getAddToCartResponseDTOS(items);
        orderResponseDTO.setItems(itemList);
        return orderResponseDTO;
    }

    private  List<AddToCartResponseDto> getAddToCartResponseDTOS(List<OrderItem> items) {
        List<AddToCartResponseDto> itemList = new ArrayList<>();
        List<Integer> itemIds = items.stream().map(OrderItem::getProductVariantId).toList();
        Map<Integer, ProductVariantDto> productVariants = productWebClient.post()
                .uri("/api/product/internal/variants/batch")
                .bodyValue(itemIds).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {
                }).block();
        for(OrderItem orderItems : items){
            ProductVariantDto productVariant = productVariants.get(orderItems.getProductVariantId());
            AddToCartResponseDto addToCartResponseDTO = new AddToCartResponseDto();
            addToCartResponseDTO.setProductVariantId(productVariant.getId());
            addToCartResponseDTO.setQuantity(orderItems.getQuantity());
//            addToCartResponseDTO.setPrice(products.getPrice());
            addToCartResponseDTO.setProductVariantName(productVariant.getProductVariantName());
            addToCartResponseDTO.setProductVariantImage(productVariant.getProductImageUrl());
            itemList.add(addToCartResponseDTO);
        }
        return itemList;
    }
}
