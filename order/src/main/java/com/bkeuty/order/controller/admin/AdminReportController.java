package com.bkeuty.order.controller.admin;

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

        com.bkeuty.order.dto.admin.DashboardDto dashboard = adminDashboardService.getDashboardData(startDate, endDate);

        switch (type.toLowerCase()) {
            case "product":
            case "category":
            case "brand":
            case "combined":
            default:
                return ResponseEntity.ok(dashboard);
        }
    }

    private boolean isAdmin(String token) {
        TokenValidationResponseDto tokenValidation = authService.validateToken(token);
        return tokenValidation.getUserId() != null && "admin".equals(tokenValidation.getUserRole());
    }
}
