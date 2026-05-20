package com.bkeuty.order.service.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.order.CreateRefundOrderRequestDto;
import com.bkeuty.order.dto.shipping.AddressDto;
import com.bkeuty.order.dto.shipping.WardDto;
import com.bkeuty.order.dto.shipping.DistrictDto;
import com.bkeuty.order.dto.shipping.ProvinceDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.entity.RefundOrder;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.RefundOrderRepository;
import com.bkeuty.order.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RefundOrderRepository refundOrderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaEventTemplate;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private RefundOrderService refundOrderService;

    private TokenValidationResponseDto userInfo;
    private CreateRefundOrderRequestDto request;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        userInfo = TokenValidationResponseDto.builder()
                .userId("user-123")
                .build();

        request = CreateRefundOrderRequestDto.builder()
                .orderId(1)
                .orderItemId(List.of(10))
                .phoneNumber("0909090909")
                .note("Sản phẩm lỗi")
                .fromAddress(AddressDto.builder()
                        .address("123 Street")
                        .ward(new WardDto(10001, "Ward A"))
                        .district(new DistrictDto(1001, "District B"))
                        .province(new ProvinceDto(101, "Province C"))
                        .build())
                .build();

        order = Order.builder()
                .id(1)
                .userId("user-123")
                .status(OrderStatus.SUCCEEDED)
                .deliveryDate(LocalDateTime.now().minusDays(2)) // 2 days ago, within 7 days
                .build();

        orderItem = OrderItem.builder()
                .id(10)
                .order(order)
                .quantity(1)
                .productVariantPrice(new BigDecimal("100000"))
                .promotionPrice(new BigDecimal("100000"))
                .voucherDiscountAmount(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createRefundOrder_ShouldSucceed_WhenRequestIsValid() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(10)).thenReturn(Optional.of(orderItem));

        RefundOrder mockSavedRefund = RefundOrder.builder()
                .id(50)
                .userId("user-123")
                .orderId(1)
                .total(new BigDecimal("100000"))
                .phoneNumber("0909090909")
                .note("Sản phẩm lỗi")
                .build();

        when(refundOrderRepository.saveAndFlush(any(RefundOrder.class))).thenReturn(mockSavedRefund);
        when(s3Service.uploadRefundEvidenceImages(anyInt(), any())).thenReturn(List.of("http://s3.url/image.png"));
        when(refundOrderRepository.save(any(RefundOrder.class))).thenReturn(mockSavedRefund);

        List<MultipartFile> dummyImages = new ArrayList<>();
        RefundOrder result = refundOrderService.createRefundOrder(userInfo, request, dummyImages);

        assertNotNull(result);
        assertEquals(50, result.getId());
        assertEquals("user-123", result.getUserId());
        assertEquals(1, result.getOrderId());
        assertEquals(new BigDecimal("100000"), result.getTotal());

        verify(orderRepository, times(1)).findById(1);
        verify(orderItemRepository, times(1)).findById(10);
        verify(refundOrderRepository, times(1)).saveAndFlush(any(RefundOrder.class));
        verify(s3Service, times(1)).uploadRefundEvidenceImages(eq(50), any());
        verify(orderItemRepository, times(1)).save(orderItem);
    }

    @Test
    void createRefundOrder_ShouldThrowNotFound_WhenOrderDoesNotExist() {
        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Order not found"));
    }

    @Test
    void createRefundOrder_ShouldThrowForbidden_WhenUserDoesNotOwnOrder() {
        order.setUserId("other-user-456");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Access denied to this order"));
    }

    @Test
    void createRefundOrder_ShouldThrowBadRequest_WhenOrderStatusNotSucceeded() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Refund is only allowed for orders that have been successfully delivered"));
    }

    @Test
    void createRefundOrder_ShouldThrowBadRequest_WhenDeliveryDateIsNull() {
        order.setDeliveryDate(null);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Order does not have a recorded delivery date"));
    }

    @Test
    void createRefundOrder_ShouldThrowBadRequest_WhenRefundWindowExpired() {
        order.setDeliveryDate(LocalDateTime.now().minusDays(8)); // Expired (window is 7 days)
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Refund window has expired"));
    }

    @Test
    void createRefundOrder_ShouldThrowBadRequest_WhenOrderItemIdListIsEmpty() {
        request.setOrderItemId(Collections.emptyList());
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("At least one order item must be selected for refund"));
    }

    @Test
    void createRefundOrder_ShouldThrowNotFound_WhenOrderItemDoesNotExist() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Order item not found"));
    }

    @Test
    void createRefundOrder_ShouldThrowBadRequest_WhenOrderItemDoesNotBelongToOrder() {
        Order otherOrder = Order.builder().id(999).build();
        orderItem.setOrder(otherOrder);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(10)).thenReturn(Optional.of(orderItem));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("does not belong to order"));
    }

    @Test
    void createRefundOrder_ShouldThrowBadRequest_WhenItemAlreadyRefunded() {
        orderItem.setRefundOrder(new RefundOrder());

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(10)).thenReturn(Optional.of(orderItem));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                refundOrderService.createRefundOrder(userInfo, request, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("has already been submitted for a refund"));
    }
}
