package com.bkeuty.order.controller.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.order.OrderResponseDto;
import com.bkeuty.order.dto.order.PlaceOrderRequestDto;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.order.OrderService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final AuthService authService;
    private final OrderService orderService;
    public OrderController(AuthService authService, OrderService orderService) {
        this.authService = authService;
        this.orderService = orderService;
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

}
