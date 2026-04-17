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
@RequestMapping("/api/admin/reports")
@Validated
public class AdminReportController {

    private final AdminDashboardService adminDashboardService;
    private final AuthService authService;

    public AdminReportController(AdminDashboardService adminDashboardService, AuthService authService) {
        this.adminDashboardService = adminDashboardService;
        this.authService = authService;
    }

    @GetMapping("/data")
    public ResponseEntity<?> getReportData(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            
        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        DashboardDto dashboard = adminDashboardService.getDashboardData(startDate, endDate, bearerToken);

        return switch (type.toLowerCase()) {
            case "product", "category", "brand", "combined" -> ResponseEntity.ok(dashboard);
            default -> new ResponseEntity<>("Invalid report type. Supported types: product, category, brand, combined.", HttpStatus.BAD_REQUEST);
        };
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
