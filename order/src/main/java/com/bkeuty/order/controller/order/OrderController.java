package com.bkeuty.order.controller.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.order.PlaceOrderRequestDto;
import com.bkeuty.order.dto.shipping.GetShippingOrderStatusRequest;
import com.bkeuty.order.dto.shipping.GetShippingOrderStatusResponseDto;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.order.OrderService;
import com.bkeuty.order.service.shipping.ShippingService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
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
    public ResponseEntity<?> findOrderByUserId(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"user".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return orderService.getListOrders(tokenValidationResponseDto.getUserId());
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
}
