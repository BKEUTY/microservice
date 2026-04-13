package com.bkeuty.order.service.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.cart.ProductVariantDto;
import com.bkeuty.order.dto.order.*;
import com.bkeuty.order.dto.shipping.*;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
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
        Integer shippingFee = shippingService.calShippingFee(CalShippingFeeDto.builder().toWardCode(request.getAddress().getWard().getWardCode().toString())
                                                                                                                               .toDistrictId(request.getAddress().getDistrict().getDistrictID())
                .serviceTypeId(2).weight(100).build())
                .block().getData().getServiceFee();

        String lastName = userInfo.getLastName() != null ? userInfo.getLastName() : "";
        String firstName = userInfo.getFirstName() != null ? userInfo.getFirstName() : "";
        String userName = (lastName + " " + firstName).trim();
        if (userName.isEmpty()) userName = "Guest";

        String shippingDate = shippingService.calShippingTime(CalShippingTimeDto.builder().toWardCode(request.getAddress().getWard().getWardCode().toString())
                .toDistrictId(request.getAddress().getDistrict().getDistrictID()).serviceTypeId(2).build()).block().getData().getLeaderTimeOrder().getToEstimateTime();
        Order order = Order.builder()
                .orderDate(LocalDate.now())
                .address(addressDtoToAddress(request.getAddress()))
                .paymentMethod(request.getPaymentMethod())
                .shippingFee(request.getShippingFee())
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
                        if(dto.getPromotionPrice() == null){
                            totalAmount = totalAmount.add(dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
                        }
                        else {
                            totalAmount = totalAmount.add(dto.getPromotionPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
                        }
                    }
                    items.add(addToCartResponseDTO);
                }
            }
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to communicate with inventory service: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Internal error processing stock: " + e.getMessage());
        }

        if (request.getShippingFee() != null) {
            totalAmount = totalAmount.add(request.getShippingFee());
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
        placeOrderResponseDTO.setStatus(PaymentStatus.UNPAID.name());
        placeOrderResponseDTO.setQrCodeLink(generateQrCode(totalAmount.add(BigDecimal.valueOf(shippingFee)), orderSave.getId()));
        
        return ResponseEntity.ok(placeOrderResponseDTO);
    }

    private String generateQrCode(BigDecimal total, Integer orderId) {
        int intTotal = total.intValue();
        return "https://qr.sepay.vn/img?acc=" + accountNumber + "&bank=" + bank + "&amount=" + intTotal + "&des=DH" + orderId + "&template=" + template + "&download=false";
    }

    public Page<OrderResponseDto> getListOrders(String userId, Pageable pageable, String status, LocalDate startDate, LocalDate endDate) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            
            if (status != null && !status.isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), PaymentStatus.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid order status: " + status);
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

        List<Integer> orderIds = pageOrders.stream().map(Order::getId).toList();
        List<OrderItem> allOrderItems = orderItemRepository.findByOrderIdIn(orderIds);
        List<Integer> variantIds = allOrderItems.stream().map(OrderItem::getProductVariantId).distinct().toList();
        Map<Integer, ProductVariantDto> productVariants = fetchVariantMap(variantIds);

        Map<Integer, List<OrderItem>> itemsByOrderId = allOrderItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<OrderResponseDto> orderResponseDTOList = pageOrders.getContent().stream()
                .map(order -> toOrderResponseDto(order, itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>()), productVariants))
                .toList();
        
        return new PageImpl<>(orderResponseDTOList, pageable, pageOrders.getTotalElements());
    }

    public OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items, Map<Integer, ProductVariantDto> productVariants) {
        OrderResponseDto orderResponseDTO = new OrderResponseDto();
        orderResponseDTO.setOrderId(order.getId() != null ? order.getId().toString() : "");
        orderResponseDTO.setUserName(order.getUserName());
        orderResponseDTO.setOrderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now());
        orderResponseDTO.setAddress(toAddressDto(order.getAddress()));
        orderResponseDTO.setPaymentMethod(order.getPaymentMethod());
        orderResponseDTO.setTotal(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        orderResponseDTO.setStatus(order.getStatus().name());
        orderResponseDTO.setShippingFee(order.getShippingFee());
        orderResponseDTO.setItems(getAddToCartResponseDTOS(items, productVariants));
        return orderResponseDTO;
    }

    public OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items) {
        List<Integer> variantIds = items.stream().map(OrderItem::getProductVariantId).distinct().toList();
        return toOrderResponseDto(order, items, fetchVariantMap(variantIds));
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
            }
            itemList.add(addToCartResponseDTO);
        }
        
        return itemList;
    }

    private Map<Integer, ProductVariantDto> fetchVariantMap(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return Collections.emptyMap();
        try {
            return productWebClient.post()
                    .uri("/api/product/internal/variants/batch")
                    .bodyValue(variantIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Integer, ProductVariantDto>>() {})
                    .block();
        } catch (Exception e) {
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
