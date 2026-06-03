package com.bkeuty.order.service.payment;

import com.bkeuty.order.dto.payment.PaymentStatusDto;
import com.bkeuty.order.dto.payment.PaymentWebhookData;
import com.bkeuty.order.dto.shipping.CreateShippingOrderDto;
import com.bkeuty.order.dto.shipping.CreateShippingOrderResponseDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.PaymentTransaction;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.PaymentTransactionRepository;
import com.bkeuty.order.service.shipping.ShippingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShippingService shippingService;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void updatePaymentTransaction_ShouldReturnTrueAndProcessOrder_WhenOrderExists() {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setContent("Thanh toan don hang DH123");
        webhookData.setId(999);
        webhookData.setTransferAmount(new BigDecimal("500000"));

        Order mockOrder = Order.builder()
                .id(123)
                .buyerName("Phong")
                .buyerNumber("0909090909")
                .address("123 Street, Ward 1, District 1, HCM|1:2:3")
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        CreateShippingOrderResponseDto shippingResponse = new CreateShippingOrderResponseDto();
        com.bkeuty.order.dto.shipping.ShippingOrderDto data = new com.bkeuty.order.dto.shipping.ShippingOrderDto();
        data.setExpectedDeliveryTime("2026-05-20T10:00:00");
        shippingResponse.setData(data);

        when(orderRepository.findById(123)).thenReturn(Optional.of(mockOrder));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.findByOrderId(123)).thenReturn(Collections.emptyList());
        when(shippingService.createShippingOrder(any(CreateShippingOrderDto.class))).thenReturn(Mono.just(shippingResponse));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Boolean result = paymentService.updatePaymentTransaction(webhookData);

        assertTrue(result);
        assertEquals(PaymentStatus.PAID, mockOrder.getPaymentStatus());
        assertEquals("2026-05-20T10:00:00", mockOrder.getEstimatedShippingDate());
        verify(paymentTransactionRepository, times(1)).save(any(PaymentTransaction.class));
        verify(shippingService, times(1)).createShippingOrder(any(CreateShippingOrderDto.class));
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void updatePaymentTransaction_ShouldReturnFalse_WhenOrderDoesNotExist() {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setContent("Thanh toan DH999"); // ID 999

        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        Boolean result = paymentService.updatePaymentTransaction(webhookData);

        assertFalse(result);
        verify(paymentTransactionRepository, never()).save(any());
        verify(shippingService, never()).createShippingOrder(any());
    }

    @Test
    void checkPaymentStatus_ShouldReturnTrue_WhenOrderIsPaid() {
        PaymentStatusDto dto = new PaymentStatusDto();
        dto.setOrderId(1);
        
        Order mockOrder = Order.builder().id(1).paymentStatus(PaymentStatus.PAID).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));

        Boolean result = paymentService.checkPaymentStatus(dto);

        assertTrue(result);
    }

    @Test
    void checkPaymentStatus_ShouldReturnFalse_WhenOrderIsUnpaid() {
        PaymentStatusDto dto = new PaymentStatusDto();
        dto.setOrderId(1);
        
        Order mockOrder = Order.builder().id(1).paymentStatus(PaymentStatus.UNPAID).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));

        Boolean result = paymentService.checkPaymentStatus(dto);

        assertFalse(result);
    }
}
