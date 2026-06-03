package com.bkeuty.order.service.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.dto.order.*;
import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.entity.CartItem;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentMethod;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.exception.CartItemNotFound;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import com.bkeuty.order.repository.CartItemRepository;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.service.shipping.ShippingService;
import com.bkeuty.order.util.OrderAddressUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient productWebClient;
    private final WebClient promotionWebClient;
    private final ShippingService  shippingService;
    @Value("${sepay.account-number:}")
    private String accountNumber;
    @Value("${sepay.bank:}")
    private String bank;
    @Value("${sepay.template:}")
    private String template;
    private final KafkaTemplate<String, DecreaseStockRequestDto> kafkaTemplate;
    private final WebClient userWebClient;
    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository, WebClient productWebClient, WebClient promotionWebClient, GHNCommunication ghnCommunication, ShippingService shippingService, KafkaTemplate<String, DecreaseStockRequestDto> kafkaTemplate, WebClient userWebClient) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productWebClient = productWebClient;
        this.promotionWebClient = promotionWebClient;
        this.shippingService = shippingService;
        this.kafkaTemplate = kafkaTemplate;
        this.userWebClient = userWebClient;
    }

    public ResponseEntity<?> placeOrder(TokenValidationResponseDto userInfo, PlaceOrderRequestDto request) {
        List<OrderCartItemDto> orderItemList = request.getOrderItems();
        if (orderItemList == null || orderItemList.isEmpty()) {
            return ResponseEntity.badRequest().body("Order items cannot be empty");
        }
        Integer trustedLevel = 0;
        try {
            Map<String, Object> userDetail = userWebClient.get()
                    .uri("/api/user/internal/{userId}", userInfo.getUserId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            if (userDetail != null && userDetail.get("membershipLevel") != null) {
                trustedLevel = (Integer) userDetail.get("membershipLevel");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch trusted membership level for user {}, defaulting to 0", userInfo.getUserId());
        }
        final Integer finalLevel = trustedLevel;

        Integer shippingFee = 30000;
        try {
            shippingFee = shippingService.calShippingFee(CalShippingFeeDto.builder()
                    .toWardCode(request.getAddress().getWard().getWardCode().toString())
                    .toDistrictId(request.getAddress().getDistrict().getDistrictID())
                    .serviceTypeId(2).weight(100).build())
                    .block().getData().getServiceFee();
        } catch (Exception e) {
            log.warn("Failed to calculate shipping fee from GHN, defaulting to 30000: {}", e.getMessage());
        }

        String shippingDate = LocalDateTime.now().plusDays(3).toString();
        try {
            shippingDate = shippingService.calShippingTime(CalShippingTimeDto.builder()
                    .toWardCode(request.getAddress().getWard().getWardCode().toString())
                    .toDistrictId(request.getAddress().getDistrict().getDistrictID())
                    .serviceTypeId(2).build())
                    .block().getData().getLeaderTimeOrder().getToEstimateTime();
        } catch (Exception e) {
            log.warn("Failed to calculate shipping time from GHN, defaulting to 3 days from now: {}", e.getMessage());
        }
        
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .address(addressDtoToAddress(request.getAddress()))
                .paymentMethod(request.getPaymentMethod())
                .userId(userInfo.getUserId())
                .userName(userInfo.getFirstName() + " " + userInfo.getLastName())
                .shippingFee(BigDecimal.valueOf(shippingFee))
                .estimatedShippingDate(shippingDate)
                .buyerName(request.getName())
                .buyerNumber(request.getPhoneNumber())
                .buyerNote(request.getNote())
                .membershipLevel(trustedLevel)
                .build();
 
        Order orderSave = orderRepository.save(order);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemDto> decreaseVariants = new ArrayList<>();
        List<AddToCartResponseDto> items = new ArrayList<>();
        List<Integer> buyVariants = new ArrayList<>();
        List<OrderItem> savedOrderItems = new ArrayList<>(); 
        List<CartItem> cartItemsToDelete = new ArrayList<>();

        for (OrderCartItemDto orderCartItemDto : orderItemList) {
            CartItem cartItems = cartItemRepository.findById(orderCartItemDto.getCartItemId())
                    .orElseThrow(() -> new CartItemNotFound("Cart item not found", orderCartItemDto.getCartItemId()));
 
            decreaseVariants.add(new OrderItemDto(cartItems.getProductVariant(), cartItems.getQuantity()));
            buyVariants.add(cartItems.getProductVariant());
            cartItemsToDelete.add(cartItems);
        }
        try {
            Map<Integer, ProductVariantDto> buyProductVariantMap = productWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/product/internal/variants/batch")
                            .queryParam("userId", userInfo.getUserId())
                            .queryParam("membershipLevel", finalLevel)
                            .build())
                    .bodyValue(buyVariants).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {
                    }).block();
            for (OrderItemDto variants : decreaseVariants) {
                if(buyProductVariantMap!=null && buyProductVariantMap.containsKey(variants.getProductVariantId()) && buyProductVariantMap.get(variants.getProductVariantId()) != null) {
                    ProductVariantDto dto = buyProductVariantMap.get(variants.getProductVariantId());
                    if (dto.getStockQuantity() < variants.getQuantity()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product '" + dto.getProductVariantName() + "' only has " + dto.getStockQuantity() + " in stock.");
                    }
                    if (dto.getPrice() != null && variants.getQuantity() != null) {
                        if(dto.getPromotionPrice() == null){
                            totalAmount = totalAmount.add(dto.getPrice().multiply(BigDecimal.valueOf(variants.getQuantity())));
                        }
                        else {
                            totalAmount = totalAmount.add(dto.getPromotionPrice().multiply(BigDecimal.valueOf(variants.getQuantity())));
                        }
                    }
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(orderSave);
                    orderItem.setProductVariantId(dto.getId());
                    orderItem.setQuantity(variants.getQuantity());
                    orderItem.setProductVariantName(dto.getProductVariantName());
                    orderItem.setProductVariantPrice(dto.getPrice());
                    orderItem.setPromotionPrice(dto.getPromotionPrice());
                    orderItem.setProductImageUrl(dto.getProductImageUrl());
                    orderItem.setProductDescription(dto.getProductVariantDescription());
                    OrderItem savedItem = orderItemRepository.save(orderItem);
                    savedOrderItems.add(savedItem);

                    AddToCartResponseDto addToCartResponseDTO = AddToCartResponseDto.builder()
                            .price(dto.getPrice())
                            .productVariantId(dto.getId())
                            .productVariantName(dto.getProductVariantName())
                            .quantity(variants.getQuantity())
                            .productVariantImage(dto.getProductImageUrl())
                            .promotionPrice(dto.getPromotionPrice())
                            .orderItemId(savedItem.getId())
                            .build();
                    items.add(addToCartResponseDTO);
                }
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to communicate with inventory service: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Internal error processing stock: " + e.getMessage());
        }
 
        BigDecimal voucherDiscountAmount = BigDecimal.ZERO;
        BigDecimal preVoucherTotal = totalAmount; 
        if (request.getVoucherId() != null && !savedOrderItems.isEmpty()) {
            try {
                voucherDiscountAmount = promotionWebClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/promotion/internal/vouchers/{voucherId}/apply")
                                .queryParam("userId", userInfo.getUserId())
                                .queryParam("membershipLevel", finalLevel)
                                .queryParam("subtotal", preVoucherTotal)
                                .build(request.getVoucherId()))
                        .retrieve()
                        .bodyToMono(BigDecimal.class)
                        .block();
                
                if (voucherDiscountAmount != null && voucherDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    orderSave.setVoucherId(request.getVoucherId());
                    orderSave.setVoucherDiscountAmount(voucherDiscountAmount);
                    
                    BigDecimal totalApportioned = BigDecimal.ZERO;
                    for (int i = 0; i < savedOrderItems.size(); i++) {
                        OrderItem item = savedOrderItems.get(i);
                        BigDecimal itemPrice = item.getPromotionPrice() != null ? item.getPromotionPrice() : item.getProductVariantPrice();
                        BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        
                        if (i == savedOrderItems.size() - 1) {
                            BigDecimal remainder = voucherDiscountAmount.subtract(totalApportioned);
                            item.setVoucherDiscountAmount(remainder);
                        } else {
                            BigDecimal itemShare = itemSubtotal.multiply(voucherDiscountAmount)
                                    .divide(preVoucherTotal, 2, RoundingMode.HALF_UP);
                            item.setVoucherDiscountAmount(itemShare);
                            totalApportioned = totalApportioned.add(itemShare);
                        }
                        orderItemRepository.save(item);
                    }
 
                    totalAmount = totalAmount.subtract(voucherDiscountAmount);
                    if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                        totalAmount = BigDecimal.ZERO;
                    }
                }
            } catch (WebClientResponseException e) {
                String body = e.getResponseBodyAsString();
                String message = body.replaceAll(".*\"message\":\"([^\"]*)\".*", "$1");
                throw new ResponseStatusException(e.getStatusCode(), message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to apply voucher: " + e.getMessage());
            }
        }
 
        orderSave.setTotal(totalAmount);
        orderRepository.save(orderSave);
 
        OrderResponseDto placeOrderResponseDTO = new OrderResponseDto();
        placeOrderResponseDTO.setOrderId(orderSave.getId().toString());
        placeOrderResponseDTO.setOrderDate(orderSave.getOrderDate());
        placeOrderResponseDTO.setShippingFee(BigDecimal.valueOf(shippingFee));
        placeOrderResponseDTO.setEstShippingDate(shippingDate);
        placeOrderResponseDTO.setAddress(request.getAddress());
        placeOrderResponseDTO.setPaymentMethod(request.getPaymentMethod());
        placeOrderResponseDTO.setTotal(totalAmount);
        placeOrderResponseDTO.setItems(items);
        placeOrderResponseDTO.setStatus(OrderStatus.NOT_CONFIRMED);
        placeOrderResponseDTO.setQrCodeLink(generateQrCode(totalAmount.add(BigDecimal.valueOf(shippingFee)), orderSave.getId()));
        placeOrderResponseDTO.setBuyerName(request.getName());
        placeOrderResponseDTO.setBuyerPhoneNumber(request.getPhoneNumber());
        placeOrderResponseDTO.setBuyerNote(request.getNote());
        placeOrderResponseDTO.setUserName(orderSave.getUserName());
        cartItemRepository.deleteAll(cartItemsToDelete);
        kafkaTemplate.send("decrease-stock-topic",new DecreaseStockRequestDto(orderSave.getId(),decreaseVariants));
 
        return ResponseEntity.ok(placeOrderResponseDTO);
//        try {
//            List<DecreaseStockResponseDto> decreaseStockResponseDtos = productWebClient.post()
//                    .uri("/api/inventory/internal/decreaseStock")
//                    .bodyValue(new DecreaseStockRequestDto(decreaseVariants))
//                    .retrieve()
//                    .bodyToMono(new ParameterizedTypeReference<List<DecreaseStockResponseDto>>() {})
//                    .block();
//
//            if (decreaseStockResponseDtos != null) {
//                for (DecreaseStockResponseDto dto : decreaseStockResponseDtos) {
//                    AddToCartResponseDto addToCartResponseDTO = AddToCartResponseDto.builder()
//                            .price(dto.getPrice())
//                            .productVariantId(dto.getProductVariantId())
//                            .productVariantName(dto.getProductVariantName())
//                            .quantity(dto.getQuantity())
//                            .productVariantImage(dto.getProductVariantImage())
//                            .promotionPrice(dto.getPromotionPrice())
//                            .build();
//
//                    if (dto.getPrice() != null && dto.getQuantity() != null) {
//                        if(dto.getPromotionPrice() == null){
//                            totalAmount = totalAmount.add(dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
//                        }
//                        else {
//                            totalAmount = totalAmount.add(dto.getPromotionPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
//                        }
//                    }
//                    OrderItem orderItem = new OrderItem();
//                    orderItem.setOrder(orderSave);
//                    orderItem.setProductVariantId(dto.getProductVariantId());
//                    orderItem.setQuantity(dto.getQuantity());
//                    orderItem.setProductVariantName(dto.getProductVariantName());
//                    orderItem.setProductVariantPrice(dto.getPrice());
//                    orderItemRepository.save(orderItem);
//                    items.add(addToCartResponseDTO);
//                }
//            }
//        } catch (WebClientResponseException e) {
//            throw new RuntimeException("Failed to communicate with inventory service: " + e.getResponseBodyAsString());
//        } catch (Exception e) {
//            throw new RuntimeException("Internal error processing stock: " + e.getMessage());
//        }


    }

    private String generateQrCode(BigDecimal total, Integer orderId) {
        int intTotal = total.intValue();
        return "https://qr.sepay.vn/img?acc=" + accountNumber + "&bank=" + bank + "&amount=" + intTotal + "&des=DH" + orderId + "&template=" + template + "&download=false";
    }

    public Page<OrderResponseDto> getListOrders(String userId, Pageable pageable, String status, String search, LocalDate startDate, LocalDate endDate) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (status != null && !status.isBlank()) {
                String trimmedStatus = status.trim();
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), OrderStatus.valueOf(trimmedStatus.toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status: " + trimmedStatus);
                }
            }
            if (search != null && !search.isBlank()) {
                String keyword = search.trim();
                List<Predicate> searchPredicates = new ArrayList<>();

                if (keyword.matches("^\\d+$")) {
                    try {
                        Integer id = Integer.parseInt(keyword);
                        searchPredicates.add(criteriaBuilder.equal(root.get("id"), id));
                    } catch (NumberFormatException ignored) {}
                } else {
                    String likePattern = "%" + keyword.toLowerCase() + "%";
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("userName"), "")), likePattern));
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("address"), "")), likePattern));
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
                }
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate.atTime(23, 59, 59, 999999999)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> pageOrders = orderRepository.findAll(spec, pageable);
        if (pageOrders.isEmpty()) {
            return Page.empty(pageable);
        }

        List<OrderResponseDto> orderResponseList = pageOrders.getContent().stream()
                .map(this::toOrderResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(orderResponseList, pageable, pageOrders.getTotalElements());
    }
    private <T> T emptyIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
    public OrderResponseDto toOrderResponseDto(Order order) {
        OrderResponseDto response = OrderResponseDto.builder()
                .orderId(order.getId() != null ? order.getId().toString() : "")
                .userName(order.getUserName())
                .orderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now())
                .address(toAddressDto(order.getAddress()))
                .paymentMethod(order.getPaymentMethod())
                .total(emptyIfNull(order.getTotal(), BigDecimal.ZERO))
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .membershipLevel(order.getMembershipLevel())
                .shippingStatus(order.getShippingStatus())
                .shippingFee(emptyIfNull(order.getShippingFee(), BigDecimal.ZERO))
                .estShippingDate(order.getEstimatedShippingDate())
                .buyerName(order.getBuyerName())
                .buyerPhoneNumber(order.getBuyerNumber())
                .buyerNote(order.getBuyerNote())
                .qrCodeLink(order.getPaymentMethod() == PaymentMethod.BANK ? generateQrCode(order.getTotal().add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO), order.getId()) : null)
                .voucherDiscountAmount(emptyIfNull(order.getVoucherDiscountAmount(), BigDecimal.ZERO))
                .build();

        List<AddToCartResponseDto> itemDtos = new ArrayList<>();
        Set<Integer> missingVariantIds = new HashSet<>();

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
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
                                    .voucherDiscountAmount(emptyIfNull(item.getVoucherDiscountAmount(), BigDecimal.ZERO))
                                    .orderItemId(item.getId())
                                    .refundOrderId(item.getRefundOrder() != null ? item.getRefundOrder().getId() : null)
                                    .refundStatus(item.getRefundOrder() != null ? item.getRefundOrder().getStatus().name() : null)
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
            // Nếu có item cũ gọi product-service
            if (!missingVariantIds.isEmpty()) {
                Map<Integer, ProductVariantDto> variants = fetchVariantMap(new ArrayList<>(missingVariantIds));

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
                                        .orderItemId(item.getId())
                                        .refundOrderId(item.getRefundOrder() != null ? item.getRefundOrder().getId() : null)
                                        .refundStatus(item.getRefundOrder() != null ? item.getRefundOrder().getStatus().name() : null)
                                        .build();
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                itemDtos.addAll(fallbackItems);
            }
        }

        response.setItems(itemDtos);
        return response;
    }
    private Map<Integer, ProductVariantDto> fetchVariantMap(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return Collections.emptyMap();
        try {
            Map<Integer, ProductVariantDto> result = productWebClient.post()
                    .uri("/api/product/internal/variants/batch")
                    .bodyValue(variantIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                    .block();
            return result != null ? result : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Failed to fetch product variants from product-service for IDs: {}", variantIds, e);
            return Collections.emptyMap();
        }
    }

    private String addressDtoToAddress(AddressDto dto) {
        return OrderAddressUtils.addressDtoToAddress(dto);
    }
    private AddressDto toAddressDto(String address) {
        return OrderAddressUtils.toAddressDto(address);
    }

    public OrderResponseDto getOrderById(Integer orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this order");
        }

        return toOrderResponseDto(order);
    }

}
