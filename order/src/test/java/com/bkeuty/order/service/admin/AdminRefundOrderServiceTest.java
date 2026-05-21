package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.AdminRefundOrderDto;
import com.bkeuty.order.dto.shipping.CreateRefundShippingMessage;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.entity.RefundOrder;
import com.bkeuty.order.enums.RefundStatus;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.RefundOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRefundOrderServiceTest {

    @Mock
    private RefundOrderRepository refundOrderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaEventTemplate;

    @Mock
    private KafkaTemplate<String, CreateRefundShippingMessage> kafkaCreateShippingOrderTemplate;

    @Mock
    private OrderRepository orderRepository;

    private AdminRefundOrderService adminRefundOrderService;

    private RefundOrder refundOrder;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        adminRefundOrderService = new AdminRefundOrderService(
                refundOrderRepository,
                kafkaEventTemplate,
                orderRepository,
                kafkaCreateShippingOrderTemplate
        );

        // Inject values for @Value fields
        ReflectionTestUtils.setField(adminRefundOrderService, "returnAddress", "123 Return St");
        ReflectionTestUtils.setField(adminRefundOrderService, "returnPhone", "0999999999");
        ReflectionTestUtils.setField(adminRefundOrderService, "returnWard", "1442:WardName");
        ReflectionTestUtils.setField(adminRefundOrderService, "returnDistrict", "201:DistrictName");
        ReflectionTestUtils.setField(adminRefundOrderService, "returnProvince", "1:ProvinceName");

        refundOrder = RefundOrder.builder()
                .id(100)
                .orderId(1)
                .userId("user-123")
                .total(new BigDecimal("150000"))
                .fromAddress("456 Sender St, O Cho Dua, Dong Da, Ha Noi|1442:201:1")
                .phoneNumber("0909090909")
                .note("Lỗi rách tem")
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        item = OrderItem.builder()
                .id(200)
                .productVariantName("Toner Centella")
                .productVariantPrice(new BigDecimal("150000"))
                .promotionPrice(new BigDecimal("150000"))
                .voucherDiscountAmount(BigDecimal.ZERO)
                .quantity(1)
                .refundOrder(refundOrder)
                .build();

        refundOrder.setOrderItems(List.of(item));
    }

    @Test
    void getRefundOrderById_ShouldReturnDto_WhenExists() {
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));
        when(orderRepository.findById(1)).thenReturn(Optional.of(Order.builder().userName("Nguyen Van A").build()));

        AdminRefundOrderDto dto = adminRefundOrderService.getRefundOrderById(100);

        assertNotNull(dto);
        assertEquals(100, dto.getRefundOrderId());
        assertEquals("Nguyen Van A", dto.getUserName());
        assertEquals(RefundStatus.PENDING, dto.getStatus());
    }

    @Test
    void getRefundOrderById_ShouldThrowNotFound_WhenNotExists() {
        when(refundOrderRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                adminRefundOrderService.getRefundOrderById(999));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Refund order not found"));
    }

    @Test
    void approveRefundOrder_ShouldSucceedAndSendKafka_WhenPending() {
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(1)).thenReturn(Optional.of(Order.builder().userName("Nguyen Van A").build()));

        AdminRefundOrderDto dto = adminRefundOrderService.approveRefundOrder(100);

        assertNotNull(dto);
        assertEquals(RefundStatus.APPROVED, dto.getStatus());

        verify(refundOrderRepository, times(1)).save(any(RefundOrder.class));
        verify(kafkaCreateShippingOrderTemplate, times(1))
                .send(eq("create-refund-shipping-topic"), any(CreateRefundShippingMessage.class));
    }

    @Test
    void approveRefundOrder_ShouldThrowBadRequest_WhenNotPending() {
        refundOrder.setStatus(RefundStatus.APPROVED);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                adminRefundOrderService.approveRefundOrder(100));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Only PENDING refund orders can be approved"));
    }

    @Test
    void rejectRefundOrder_ShouldSucceed_WhenPending() {
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminRefundOrderDto dto = adminRefundOrderService.rejectRefundOrder(100);

        assertNotNull(dto);
        assertEquals(RefundStatus.REJECTED, dto.getStatus());
        verify(refundOrderRepository, times(1)).save(any(RefundOrder.class));
    }

    @Test
    void rejectRefundOrder_ShouldThrowBadRequest_WhenNotPending() {
        refundOrder.setStatus(RefundStatus.REJECTED);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                adminRefundOrderService.rejectRefundOrder(100));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Only PENDING refund orders can be rejected"));
    }

    @Test
    void completeRefundOrder_ShouldSucceed_WhenDelivered() {
        refundOrder.setStatus(RefundStatus.DELIVERED);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminRefundOrderDto dto = adminRefundOrderService.completeRefundOrder(100);

        assertNotNull(dto);
        assertEquals(RefundStatus.DELIVERED, dto.getStatus());
        verify(refundOrderRepository, times(1)).save(any(RefundOrder.class));
    }

    @Test
    void completeRefundOrder_ShouldSucceed_WhenApproved() {
        refundOrder.setStatus(RefundStatus.APPROVED);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminRefundOrderDto dto = adminRefundOrderService.completeRefundOrder(100);

        assertNotNull(dto);
        assertEquals(RefundStatus.DELIVERED, dto.getStatus());
        verify(refundOrderRepository, times(1)).save(any(RefundOrder.class));
    }

    @Test
    void completeRefundOrder_ShouldThrowBadRequest_WhenNotDelivered() {
        refundOrder.setStatus(RefundStatus.PENDING);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                adminRefundOrderService.completeRefundOrder(100));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Only APPROVED refund orders can be marked as COMPLETED"));
    }

    @Test
    void processMoneyRefund_ShouldSendKafkaEvent_WhenDelivered() {
        refundOrder.setStatus(RefundStatus.DELIVERED);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminRefundOrderDto dto = adminRefundOrderService.processMoneyRefund(100);

        assertNotNull(dto);
        assertEquals(RefundStatus.REFUNDING, dto.getStatus());
        verify(refundOrderRepository, times(1)).save(any(RefundOrder.class));
        verify(kafkaEventTemplate, times(1))
                .send(eq("process-refund-topic"), any());
    }

    @Test
    void processMoneyRefund_ShouldThrowBadRequest_WhenNotDelivered() {
        refundOrder.setStatus(RefundStatus.PENDING);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                adminRefundOrderService.processMoneyRefund(100));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Money refund can only be processed for COMPLETED refund orders"));
    }

    @Test
    void markRefunded_ShouldTransitionToRefunded_WhenNotRefundedYet() {
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));

        adminRefundOrderService.markRefunded(100);

        assertEquals(RefundStatus.REFUNDED, refundOrder.getStatus());
        verify(refundOrderRepository, times(1)).save(refundOrder);
    }

    @Test
    void markRefunded_ShouldDoNothing_WhenAlreadyRefunded() {
        refundOrder.setStatus(RefundStatus.REFUNDED);
        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(refundOrder));

        adminRefundOrderService.markRefunded(100);

        verify(refundOrderRepository, never()).save(any());
    }
}
