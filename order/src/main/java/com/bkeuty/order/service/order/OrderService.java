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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final ShippingService  shippingService;
    @Value("${sepay.account-number:}")
    private String accountNumber;
    @Value("${sepay.bank:}")
    private String bank;
    @Value("${sepay.template:}")
    private String template;
    private final KafkaTemplate<String, DecreaseStockRequestDto> kafkaTemplate;
    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository, WebClient productWebClient, GHNCommunication ghnCommunication, ShippingService shippingService, KafkaTemplate<String, DecreaseStockRequestDto> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productWebClient = productWebClient;
        this.shippingService = shippingService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public ResponseEntity<?> placeOrder(TokenValidationResponseDto userInfo, PlaceOrderRequestDto request) {
        List<OrderCartItemDto> orderItemList = request.getOrderItems();
        if (orderItemList == null || orderItemList.isEmpty()) {
            return ResponseEntity.badRequest().body("Order items cannot be empty");
        }
        Integer shippingFee = shippingService.calShippingFee(CalShippingFeeDto.builder().toWardCode(request.getAddress().getWard().getWardCode().toString())
                                                                                                                               .toDistrictId(request.getAddress().getDistrict().getDistrictID())
                .serviceTypeId(2).weight(100).build())
                .block().getData().getServiceFee();

        String shippingDate = shippingService.calShippingTime(CalShippingTimeDto.builder().toWardCode(request.getAddress().getWard().getWardCode().toString())
                .toDistrictId(request.getAddress().getDistrict().getDistrictID()).serviceTypeId(2).build()).block().getData().getLeaderTimeOrder().getToEstimateTime();
        Order order = Order.builder()
                .orderDate(java.time.LocalDateTime.now())
                .address(addressDtoToAddress(request.getAddress()))
                .paymentMethod(request.getPaymentMethod())
                .userId(userInfo.getUserId())
                .userName(userInfo.getFirstName() + " " + userInfo.getLastName())
                .shippingFee(BigDecimal.valueOf(shippingFee))
                .estimatedShippingDate(shippingDate)
                .buyerName(request.getName())
                .buyerNumber(request.getPhoneNumber())
                .buyerNote(request.getNote())
                .build();

        Order orderSave = orderRepository.save(order);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemDto> decreaseVariants = new ArrayList<>();
        List<AddToCartResponseDto> items = new ArrayList<>();
        List<Integer> buyVariants = new ArrayList<>();
        for (OrderCartItemDto orderCartItemDto : orderItemList) {
            CartItem cartItems = cartItemRepository.findById(orderCartItemDto.getCartItemId())
                    .orElseThrow(() -> new CartItemNotFound("Cart item not found", orderCartItemDto.getCartItemId()));

            decreaseVariants.add(new OrderItemDto(cartItems.getProductVariant(), cartItems.getQuantity()));


            buyVariants.add(cartItems.getProductVariant());
            cartItemRepository.delete(cartItems);
        }
        try {
            Map<Integer, ProductVariantDto> buyProductVariantMap = productWebClient.post()
                    .uri("/api/product/internal/variants/batch")
                    .bodyValue(buyVariants).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {
                    }).block();
            for (OrderItemDto variants : decreaseVariants) {
                if(buyProductVariantMap!=null && buyProductVariantMap.containsKey(variants.getProductVariantId()) && buyProductVariantMap.get(variants.getProductVariantId()) != null) {
                    System.out.println("Create Order Item");
                    ProductVariantDto dto = buyProductVariantMap.get(variants.getProductVariantId());
                    if (dto.getStockQuantity() < variants.getQuantity()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product '" + dto.getProductVariantName() + "' only has " + dto.getStockQuantity() + " in stock.");
                    }
                    AddToCartResponseDto addToCartResponseDTO = AddToCartResponseDto.builder()
                            .price(dto.getPrice())
                            .productVariantId(dto.getId())
                            .productVariantName(dto.getProductVariantName())
                            .quantity(variants.getQuantity())
                            .productVariantImage(dto.getProductImageUrl())
                            .promotionPrice(dto.getPromotionPrice())
                            .build();
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
                    orderItemRepository.save(orderItem);
                    items.add(addToCartResponseDTO);
                }
                else {
                    System.out.println("Item is null");
                }

            }
        } catch (ResponseStatusException e) {
            throw e;
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
                .orderDate(order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : LocalDate.now())
                .address(toAddressDto(order.getAddress()))
                .paymentMethod(order.getPaymentMethod())
                .total(emptyIfNull(order.getTotal(), BigDecimal.ZERO))
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .shippingStatus(order.getShippingStatus())
                .shippingFee(order.getShippingFee())
                .estShippingDate(order.getEstimatedShippingDate())
                .buyerName(order.getBuyerName())
                .buyerPhoneNumber(order.getBuyerNumber())
                .buyerNote(order.getBuyerNote())
                .qrCodeLink(order.getPaymentMethod() == PaymentMethod.BANK ? generateQrCode(order.getTotal().add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO), order.getId()) : null)
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
    public OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items) {
        OrderResponseDto orderResponseDTO = new OrderResponseDto();
        orderResponseDTO.setOrderId(order.getId() != null ? order.getId().toString() : "");
        orderResponseDTO.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : LocalDate.now());
        orderResponseDTO.setAddress(toAddressDto(order.getAddress()));
        orderResponseDTO.setPaymentMethod(order.getPaymentMethod());
        orderResponseDTO.setTotal(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        orderResponseDTO.setStatus(order.getStatus());
        orderResponseDTO.setShippingFee(order.getShippingFee());
        orderResponseDTO.setPaymentMethod(order.getPaymentMethod());
        orderResponseDTO.setPaymentStatus(order.getPaymentStatus());
        orderResponseDTO.setShippingStatus(order.getShippingStatus());
        orderResponseDTO.setItems(getAddToCartResponseDTOS(items));
        return orderResponseDTO;
    }

    private List<AddToCartResponseDto> getAddToCartResponseDTOS(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return new ArrayList<>();

        List<AddToCartResponseDto> itemList = new ArrayList<>();
        List<Integer> itemIds = items.stream().map(OrderItem::getProductVariantId).toList();
        Map<Integer, ProductVariantDto> productVariants = fetchVariantMap(itemIds);

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
        
        return itemList;
    }
    private String addressDtoToAddress(AddressDto dto) {
        return dto.getAddress()+", "+dto.getWard().getWardName() + ", "+ dto.getDistrict().getDistrictName()+  ", "+ dto.getProvince().getProvinceName()
                + "|" + dto.getWard().getWardCode().toString()
                + ":" + dto.getDistrict().getDistrictID().toString()
                + ":" + dto.getProvince().getProvinceID().toString();
    }
    private AddressDto toAddressDto(String address) {
        String[] addressArray = address.split("\\|");
        if(addressArray.length!=2) return null;
        String nameField = addressArray[0];
        String codeField = addressArray[1];
        String[] nameArray = nameField.split(",\\s*");
        if(nameArray.length< 4) return null;
        int nameLength = nameArray.length;

        StringBuilder addressName  = new StringBuilder();
        for(int nameIndex=0;nameIndex<nameLength-3;nameIndex++){
            if(nameIndex>0) addressName.append(", ");
            addressName.append(nameArray[nameIndex]);
        }

        String wardName  = nameArray[nameLength-3];
        String districtName = nameArray[nameLength-2];
        String provinceName = nameArray[nameLength-1];
        String[] codeArray = codeField.split(":");
        if(codeArray.length!=3) return null;
        return AddressDto.builder()
                .address(addressName.toString())
                .ward(new WardDto(Integer.valueOf(codeArray[0]), wardName))
                .district(new DistrictDto(Integer.valueOf(codeArray[1]), districtName))
                .province(new ProvinceDto(Integer.valueOf(codeArray[2]), provinceName))
                .build();
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
