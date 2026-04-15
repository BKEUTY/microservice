package com.bkeuty.order.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.dto.order.DecreaseStockRequestDto;
import com.bkeuty.order.dto.order.OrderCartItemDto;
import com.bkeuty.order.dto.order.OrderItemDto;
import com.bkeuty.order.dto.order.OrderResponseDto;
import com.bkeuty.order.dto.order.PlaceOrderRequestDto;
import com.bkeuty.order.dto.shipping.AddressDto;
import com.bkeuty.order.dto.shipping.CalShippingFeeDto;
import com.bkeuty.order.dto.shipping.CalShippingTimeDto;
import com.bkeuty.order.dto.shipping.DistrictDto;
import com.bkeuty.order.dto.shipping.ProvinceDto;
import com.bkeuty.order.dto.shipping.WardDto;
import com.bkeuty.order.entity.CartItem;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.exception.CartItemNotFound;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import com.bkeuty.order.repository.CartItemRepository;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.service.shipping.ShippingService;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

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

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository, WebClient productWebClient, GHNCommunication ghnCommunication, ShippingService shippingService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productWebClient = productWebClient;
        this.shippingService = shippingService;
    }

    public ResponseEntity<?> placeOrder(TokenValidationResponseDto userInfo, PlaceOrderRequestDto request) {
        List<OrderCartItemDto> orderItemList = request.getOrderItems();
        if (orderItemList == null || orderItemList.isEmpty()) {
            return ResponseEntity.badRequest().body("Order items cannot be empty");
        }
        Integer shippingFee = shippingService.calShippingFee(CalShippingFeeDto.builder()
                .toWardCode(request.getAddress().getWard().getWardCode().toString())
                .toDistrictId(request.getAddress().getDistrict().getDistrictID())
                .serviceTypeId(2).weight(100).build())
                .block().getData().getServiceFee();

        String lastName = userInfo.getLastName() != null ? userInfo.getLastName() : "";
        String firstName = userInfo.getFirstName() != null ? userInfo.getFirstName() : "";
        String userName = (lastName + " " + firstName).trim();
        if (userName.isEmpty()) userName = "Guest";

        String shippingDate = shippingService.calShippingTime(CalShippingTimeDto.builder()
                .toWardCode(request.getAddress().getWard().getWardCode().toString())
                .toDistrictId(request.getAddress().getDistrict().getDistrictID()).serviceTypeId(2).build())
                .block().getData().getLeaderTimeOrder().getToEstimateTime();
        Order order = Order.builder()
                .orderDate(LocalDate.now())
                .address(addressDtoToAddress(request.getAddress()))
                .paymentMethod(request.getPaymentMethod())
                .userId(userInfo.getUserId())
                .userName(userName)
                .shippingFee(BigDecimal.valueOf(shippingFee))
                .estimatedShippingDate(shippingDate)
                .status(PaymentStatus.UNPAID)
                .build();

        Order orderSave = orderRepository.save(order);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemDto> decreaseVariants = new ArrayList<>();
        List<AddToCartResponseDto> items = new ArrayList<>();
        List<OrderItem> orderItemsToSave = new ArrayList<>();
        List<Integer> variantIds = new ArrayList<>();
        List<Integer> cartItemIds = orderItemList.stream()
                .map(OrderCartItemDto::getCartItemId)
                .collect(Collectors.toList());
        
        if (cartItemIds.stream().anyMatch(id -> id == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cart item ID cannot be null");
        }
        
        if (cartItemIds.size() != cartItemIds.stream().distinct().count()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Duplicate cart item ID in request");
        }
        
        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
        
        if (cartItems.size() != cartItemIds.size()) {
            throw new CartItemNotFound("One or more cart items not found", null);
        }
        
        if (cartItems.stream().anyMatch(item -> !userInfo.getUserId().equals(item.getUserId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "One or more cart items do not belong to the authenticated user");
        }
        
        Map<Integer, CartItem> cartItemMap = cartItems.stream()
                .collect(Collectors.toMap(CartItem::getId, item -> item));
        
        for (OrderCartItemDto cartItemDto : orderItemList) {
            CartItem cartItem = cartItemMap.get(cartItemDto.getCartItemId());
            
            if (cartItem == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Cart item not found in map: " + cartItemDto.getCartItemId());
            }
            
            if (cartItem.getProductVariant() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Product variant ID is missing for cart item: " + cartItem.getId());
            }
            
            if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid quantity for cart item: " + cartItem.getId());
            }
            
            variantIds.add(cartItem.getProductVariant());
            decreaseVariants.add(new OrderItemDto(cartItem.getProductVariant(), cartItem.getQuantity()));
        }

        Map<Integer, ProductVariantDto> variants = fetchVariantMap(
                variantIds.stream().distinct().collect(Collectors.toList()));

        for (OrderCartItemDto cartItemDto : orderItemList) {
            CartItem cartItem = cartItemMap.get(cartItemDto.getCartItemId());
            
            if (cartItem == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Cart item not found in map: " + cartItemDto.getCartItemId());
            }
            
            if (cartItem.getProductVariant() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Product variant ID is missing for cart item: " + cartItem.getId());
            }

            ProductVariantDto variantDto = variants.get(cartItem.getProductVariant());
            if (variantDto == null) {
                if (variants.isEmpty() && !variantIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, 
                        "Product service temporarily unavailable");
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Product variant not found: " + cartItem.getProductVariant());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(orderSave)
                    .productVariantId(cartItem.getProductVariant())
                    .productVariantName(variantDto.getProductVariantName())
                    .productImageUrl(variantDto.getProductImageUrl())
                    .price(variantDto.getPrice())
                    .promotionPrice(variantDto.getPromotionPrice())
                    .productDescription(variantDto.getProductVariantDescription())
                    .quantity(cartItem.getQuantity())
                    .isReviewed(false)
                    .build();

            orderItemsToSave.add(orderItem);
            cartItemRepository.delete(cartItem);

            BigDecimal effectivePrice = variantDto.getPromotionPrice() != null && 
                    variantDto.getPromotionPrice().compareTo(variantDto.getPrice()) < 0
                    ? variantDto.getPromotionPrice() 
                    : variantDto.getPrice();
            totalAmount = totalAmount.add(effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            AddToCartResponseDto itemDto = AddToCartResponseDto.builder()
                    .productVariantId(variantDto.getId())
                    .productVariantName(variantDto.getProductVariantName())
                    .productVariantImage(variantDto.getProductImageUrl())
                    .price(variantDto.getPrice())
                    .promotionPrice(variantDto.getPromotionPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            items.add(itemDto);
        }

        orderItemRepository.saveAll(orderItemsToSave);

        try {
            productWebClient.post()
                    .uri("/api/inventory/internal/decreaseStock")
                    .bodyValue(new DecreaseStockRequestDto(decreaseVariants))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            HttpStatus statusCode = HttpStatus.valueOf(e.getStatusCode().value());
            
            if (statusCode == HttpStatus.CONFLICT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                        "Insufficient stock for items", e);
            } else if (statusCode == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Product variant not found", e);
            } else if (statusCode.is5xxServerError()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Inventory service error, please try again", e);
            } else {
                throw new ResponseStatusException(statusCode, "Failed to update inventory", e);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Internal error processing stock: " + e.getMessage());
        }

        totalAmount = totalAmount.add(BigDecimal.valueOf(shippingFee));
        orderSave.setTotal(totalAmount);
        orderRepository.save(orderSave);
        OrderResponseDto response = OrderResponseDto.builder()
                .orderId(orderSave.getId().toString())
                .orderDate(LocalDate.now())
                .shippingFee(BigDecimal.valueOf(shippingFee))
                .estShippingDate(shippingDate)
                .address(request.getAddress())
                .paymentMethod(request.getPaymentMethod())
                .total(totalAmount)
                .items(items)
                .status(PaymentStatus.UNPAID.name())
                .qrCodeLink(generateQrCode(totalAmount, orderSave.getId()))
                .build();

        return ResponseEntity.ok(response);
    }

    private String generateQrCode(BigDecimal total, Integer orderId) {
        int intTotal = total.intValue();
        return "https://qr.sepay.vn/img?acc=" + accountNumber + "&bank=" + bank + "&amount=" + intTotal + "&des=DH" + orderId + "&template=" + template + "&download=false";
    }

    public Page<OrderResponseDto> getListOrders(String userId, Pageable pageable, String status, LocalDate startDate, LocalDate endDate) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (status != null && !status.isBlank()) {
                String trimmedStatus = status.trim();
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), PaymentStatus.valueOf(trimmedStatus.toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status: " + trimmedStatus + ". Allowed values: " + java.util.Arrays.toString(PaymentStatus.values()));
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

        Page<Order> pageOrders = orderRepository.findAll(spec, pageable);
        if (pageOrders.isEmpty()) {
            return Page.empty(pageable);
        }

        List<OrderResponseDto> orderResponseList = pageOrders.getContent().stream()
                .map(this::toOrderResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(orderResponseList, pageable, pageOrders.getTotalElements());
    }

    public OrderResponseDto toOrderResponseDto(Order order) {
        OrderResponseDto response = new OrderResponseDto();
        response.setOrderId(order.getId() != null ? order.getId().toString() : "");
        response.setUserName(order.getUserName());
        response.setOrderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now());
        response.setAddress(toAddressDto(order.getAddress()));
        response.setPaymentMethod(order.getPaymentMethod());
        response.setTotal(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        response.setStatus(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name());
        response.setShippingFee(order.getShippingFee());
        response.setEstShippingDate(order.getEstimatedShippingDate());

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
                    .filter(dto -> dto.getProductVariantName() != null && !dto.getProductVariantName().isBlank())
                    .collect(Collectors.toList())
                : new ArrayList<>();
        
        if (itemDtos.isEmpty() && order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<Integer> variantIds = order.getOrderItems().stream()
                    .map(OrderItem::getProductVariantId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!variantIds.isEmpty()) {
                Map<Integer, ProductVariantDto> variants = fetchVariantMap(variantIds);
                itemDtos = order.getOrderItems().stream()
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
            }
        }

        response.setItems(itemDtos);
        return response;
    }

    public OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items, Map<Integer, ProductVariantDto> productVariants) {
        OrderResponseDto orderResponseDTO = new OrderResponseDto();
        orderResponseDTO.setOrderId(order.getId() != null ? order.getId().toString() : "");
        orderResponseDTO.setUserName(order.getUserName());
        orderResponseDTO.setOrderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now());
        orderResponseDTO.setAddress(toAddressDto(order.getAddress()));
        orderResponseDTO.setPaymentMethod(order.getPaymentMethod());
        orderResponseDTO.setTotal(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        orderResponseDTO.setStatus(order.getStatus() != null ? order.getStatus().name() : PaymentStatus.UNPAID.name());
        orderResponseDTO.setShippingFee(order.getShippingFee());
        orderResponseDTO.setItems(getAddToCartResponseDTOS(items, productVariants));
        return orderResponseDTO;
    }

    public OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items) {
        List<OrderItem> safeItems = items != null ? items : Collections.emptyList();
        List<Integer> variantIds = safeItems.stream().map(OrderItem::getProductVariantId).distinct().toList();
        return toOrderResponseDto(order, safeItems, fetchVariantMap(variantIds));
    }

    private List<AddToCartResponseDto> getAddToCartResponseDTOS(List<OrderItem> items, Map<Integer, ProductVariantDto> productVariants) {
        if (items == null || items.isEmpty()) return new ArrayList<>();

        List<AddToCartResponseDto> itemList = new ArrayList<>();
        
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
            } else {
                addToCartResponseDTO.setProductVariantName(orderItems.getProductVariantName());
                addToCartResponseDTO.setProductVariantImage(orderItems.getProductImageUrl());
                addToCartResponseDTO.setPrice(orderItems.getPrice());
                addToCartResponseDTO.setPromotionPrice(orderItems.getPromotionPrice());
            }
            itemList.add(addToCartResponseDTO);
        }
        
        return itemList;
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
        return dto.getAddress()+", "+dto.getWard().getWardName() + ", "+ dto.getDistrict().getDistrictName()+  ", "+ dto.getProvince().getProvinceName()
                + "|" + dto.getWard().getWardCode().toString()
                + ":" + dto.getDistrict().getDistrictID().toString()
                + ":" + dto.getProvince().getProvinceID().toString();
    }
    private AddressDto toAddressDto(String address) {
        AddressDto addressDto = new AddressDto();
        String[] addressArray = address.split("\\|");
        if(addressArray.length!=2){
            return null;
        }
        String nameField = addressArray[0];
        String codeField = addressArray[1];
        String[] nameArray = nameField.split(",\\s*");
        if(nameArray.length< 4){
            return null;
        }
        int nameLength = nameArray.length;

        StringBuilder addressName = new StringBuilder();
        for (int nameIndex = 0; nameIndex < nameLength - 3; nameIndex++) {
            if (nameIndex > 0) {
                addressName.append(", ");
            }
            addressName.append(nameArray[nameIndex]);
        }

        String wardName  = nameArray[nameLength-3];
        String districtName = nameArray[nameLength-2];
        String provinceName = nameArray[nameLength-1];
        String[] codeArray = codeField.split(":");
        if(codeArray.length!=3){
            return null;
        }
        String wardCode = codeArray[0];
        String districtCode = codeArray[1];
        String provinceCode = codeArray[2];
        return AddressDto.builder()
                .address(addressName.toString())
                .ward(new WardDto(Integer.valueOf(wardCode), wardName))
                .district(new DistrictDto(Integer.valueOf(districtCode), districtName))
                .province(new ProvinceDto(Integer.valueOf(provinceCode), provinceName))
                .build();
    }

}
