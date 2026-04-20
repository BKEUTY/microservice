package com.bkeuty.order.controller.admin;

import com.bkeuty.order.dto.admin.DashboardDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.service.admin.AdminDashboardService;
import com.bkeuty.order.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
@Validated
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AuthService authService;

    public AdminDashboardController(AdminDashboardService adminDashboardService, AuthService authService) {
        this.adminDashboardService = adminDashboardService;
        this.authService = authService;
    }

    @GetMapping({"", "/data"})
    public ResponseEntity<DashboardDto> getDashboardOverview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(adminDashboardService.getDashboardData(startDate, endDate, bearerToken));
    }

    @GetMapping("/details/orders")
    public ResponseEntity<?> getDetailedOrders(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(adminDashboardService.getDetailedOrders(startDate, endDate, bearerToken));
    }

    @GetMapping("/details/products")
    public ResponseEntity<?> getDetailedProducts(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(adminDashboardService.getDetailedProducts(startDate, endDate, bearerToken));
    }

    @GetMapping("/details/customers")
    public ResponseEntity<?> getDetailedCustomers(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(adminDashboardService.getDetailedCustomers(startDate, endDate, bearerToken));
    }

    @GetMapping("/details/new-customers")
    public ResponseEntity<?> getDetailedNewUsers(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(adminDashboardService.getDetailedNewUsers(startDate, endDate, bearerToken));
    }

    private boolean isAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        try {
            TokenValidationResponseDto tokenValidation = authService.validateToken(token);
            return tokenValidation != null 
                    && tokenValidation.getUserId() != null 
                    && "admin".equalsIgnoreCase(tokenValidation.getUserRole());
        } catch (Exception e) {
            return false;
        }
    }
}
