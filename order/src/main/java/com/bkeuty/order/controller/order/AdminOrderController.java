package com.bkeuty.order.controller.admin;

import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.dto.admin.AdminUpdateOrderStatusRequestDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.order.AdminOrderService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final AuthService authService;

    public AdminOrderController(AdminOrderService adminOrderService, AuthService authService) {
        this.adminOrderService = adminOrderService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminOrderDto>> getAllOrders(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        TokenValidationResponseDto tokenValidation = authService.validateToken(bearerToken);
        if (tokenValidation.getUserId() == null || tokenValidation.getUserRole() == null
                || !"admin".equals(tokenValidation.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        return ResponseEntity.ok(adminOrderService.getAllOrders(PageRequest.of(page, size)));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderDto> updateOrderStatus(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer orderId,
            @RequestBody AdminUpdateOrderStatusRequestDto request) {
            
        TokenValidationResponseDto tokenValidation = authService.validateToken(bearerToken);
        if (tokenValidation.getUserId() == null || tokenValidation.getUserRole() == null
                || !"admin".equals(tokenValidation.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(orderId, request.getStatus()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDto> getOrderById(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer orderId) {
            
        TokenValidationResponseDto tokenValidation = authService.validateToken(bearerToken);
        if (tokenValidation.getUserId() == null || tokenValidation.getUserRole() == null
                || !"admin".equals(tokenValidation.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        return ResponseEntity.ok(adminOrderService.getOrderById(orderId));
    }
}
