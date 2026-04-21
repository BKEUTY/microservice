package com.bkeuty.order.controller.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.order.OrderResponseDto;
import com.bkeuty.order.dto.order.PlaceOrderRequestDto;
import com.bkeuty.order.dto.shipping.GetShippingOrderStatusRequest;
import com.bkeuty.order.dto.shipping.GetShippingOrderStatusResponseDto;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.order.OrderService;
import com.bkeuty.order.util.OrderSortUtils;
import com.bkeuty.order.service.shipping.ShippingService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/order")
@Validated
public class OrderController {

    private final AuthService authService;
    private final OrderService orderService;
    private final ShippingService shippingService;
    public OrderController(AuthService authService, OrderService orderService, ShippingService shippingService) {
        this.authService = authService;
        this.orderService = orderService;
        this.shippingService = shippingService;
    }

    @GetMapping("/history")
    public ResponseEntity<Page<OrderResponseDto>> findOrderByUserId(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"user".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Sort sortObj = OrderSortUtils.parseSort(sort);

        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        return ResponseEntity.ok(orderService.getListOrders(
                tokenValidationResponseDto.getUserId(),
                pageable,
                status,
                search,
                startDate,
                endDate));
    }

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody PlaceOrderRequestDto placeOrderRequest) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId() == null||tokenValidationResponseDto.getUserRole()==null || !"user".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return orderService.placeOrder(tokenValidationResponseDto,placeOrderRequest);
    }
    @PostMapping("/shipping-status")
    public ResponseEntity<GetShippingOrderStatusResponseDto> getShippingStatus(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken, @RequestBody GetShippingOrderStatusRequest dto) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto.getUserId() == null||tokenValidationResponseDto.getUserRole()==null || !"user".equals(tokenValidationResponseDto.getUserRole())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().body(shippingService.getShippingOrderStatus(dto,tokenValidationResponseDto));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer orderId) {

        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"user".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(orderService.getOrderById(orderId, tokenValidationResponseDto.getUserId()));
    }
}
