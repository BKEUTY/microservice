package com.bkeuty.order.controller.admin;

import com.bkeuty.order.dto.admin.AdminOrderDto;
import com.bkeuty.order.dto.admin.AdminUpdateOrderStatusRequestDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.service.auth.AuthService;
import com.bkeuty.order.service.admin.AdminOrderService;
import com.bkeuty.order.util.OrderSortUtils;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/order")
@Validated
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
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        Sort sortObj = OrderSortUtils.parseSort(sort);
        
        return ResponseEntity.ok(adminOrderService.getAllOrders(
                PageRequest.of(page - 1, size, sortObj), 
                status, 
                search,
                startDate, 
                endDate,
                bearerToken));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDto> getOrderById(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer orderId) {
            
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        
        return ResponseEntity.ok(adminOrderService.getOrderById(orderId, bearerToken));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderDto> updateOrderStatus(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer orderId,
            @RequestBody AdminUpdateOrderStatusRequestDto request) {
            
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(orderId, request.getStatus(), bearerToken));
    }

    private boolean isAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        try {
            TokenValidationResponseDto tokenValidation = authService.validateToken(token);
            return tokenValidation != null && tokenValidation.getUserId() != null && "admin".equalsIgnoreCase(tokenValidation.getUserRole());
        } catch (Exception e) {
            return false;
        }
    }
}
