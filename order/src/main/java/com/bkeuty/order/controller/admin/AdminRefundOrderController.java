package com.bkeuty.order.controller.admin;

import com.bkeuty.order.dto.admin.AdminRefundOrderDto;
import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.service.admin.AdminRefundOrderService;
import com.bkeuty.order.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/refund-order")
@Validated
public class AdminRefundOrderController {

    private final AdminRefundOrderService adminRefundOrderService;
    private final AuthService authService;

    public AdminRefundOrderController(AdminRefundOrderService adminRefundOrderService,
                                      AuthService authService) {
        this.adminRefundOrderService = adminRefundOrderService;
        this.authService = authService;
    }

    // ------------------------------------------------------------------
    // GET /api/admin/refund-order
    // ------------------------------------------------------------------

    /**
     * Returns a paginated list of all refund orders.
     *
     * @param status optional filter by {@code RefundStatus} name (case-insensitive)
     */
    @GetMapping
    public ResponseEntity<Page<AdminRefundOrderDto>> getAllRefundOrders(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(1000) int size,
            @RequestParam(name = "status", required = false) String status) {

        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(adminRefundOrderService.getAllRefundOrders(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")),
                status));
    }

    // ------------------------------------------------------------------
    // GET /api/admin/refund-order/{refundOrderId}
    // ------------------------------------------------------------------

    @GetMapping("/{refundOrderId}")
    public ResponseEntity<AdminRefundOrderDto> getRefundOrderById(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "refundOrderId") Integer refundOrderId) {

        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(adminRefundOrderService.getRefundOrderById(refundOrderId));
    }

    // ------------------------------------------------------------------
    // PUT /api/admin/refund-order/{refundOrderId}/approve
    // ------------------------------------------------------------------

    /**
     * Approves a PENDING refund order → status becomes APPROVED.
     */
    @PutMapping("/{refundOrderId}/approve")
    public ResponseEntity<AdminRefundOrderDto> approveRefundOrder(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "refundOrderId") Integer refundOrderId) {

        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(adminRefundOrderService.approveRefundOrder(refundOrderId));
    }

    // ------------------------------------------------------------------
    // PUT /api/admin/refund-order/{refundOrderId}/reject
    // ------------------------------------------------------------------

    /**
     * Rejects a PENDING refund order → status becomes REJECTED.
     */
    @PutMapping("/{refundOrderId}/reject")
    public ResponseEntity<AdminRefundOrderDto> rejectRefundOrder(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "refundOrderId") Integer refundOrderId) {

        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(adminRefundOrderService.rejectRefundOrder(refundOrderId));
    }

    // ------------------------------------------------------------------
    // PUT /api/admin/refund-order/{refundOrderId}/complete
    // ------------------------------------------------------------------

    /**
     * Marks an APPROVED refund order as COMPLETED (physical return received).
     */
    @PutMapping("/{refundOrderId}/complete")
    public ResponseEntity<AdminRefundOrderDto> completeRefundOrder(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "refundOrderId") Integer refundOrderId) {

        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(adminRefundOrderService.completeRefundOrder(refundOrderId));
    }

    // ------------------------------------------------------------------
    // POST /api/admin/refund-order/{refundOrderId}/process-refund
    // ------------------------------------------------------------------

    /**
     * Triggers the money refund for a COMPLETED refund order.
     * Publishes a Kafka event to {@code process-refund-topic}; User Service
     * credits the wallet and replies on {@code refund-wallet-success-topic},
     * at which point the status transitions to REFUNDED.
     */
    @PostMapping("/{refundOrderId}/process-refund")
    public ResponseEntity<AdminRefundOrderDto> processMoneyRefund(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "refundOrderId") Integer refundOrderId) {

        if (!isAdmin(bearerToken)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return ResponseEntity.ok(adminRefundOrderService.processMoneyRefund(refundOrderId));
    }

    // ------------------------------------------------------------------
    // Auth helper
    // ------------------------------------------------------------------

    private boolean isAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        try {
            TokenValidationResponseDto tv = authService.validateToken(token);
            return tv != null && tv.getUserId() != null && "admin".equalsIgnoreCase(tv.getUserRole());
        } catch (Exception e) {
            return false;
        }
    }
}
