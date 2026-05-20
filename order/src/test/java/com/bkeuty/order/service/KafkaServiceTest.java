package com.bkeuty.order.service;

import com.bkeuty.order.dto.order.RefundWalletSuccessEventDto;
import com.bkeuty.order.dto.shipping.CreateShippingOrderMessage;
import com.bkeuty.order.dto.shipping.CreateShippingOrderResponseDto;
import com.bkeuty.order.dto.shipping.CreateShippingResponseMessage;
import com.bkeuty.order.dto.shipping.GhnWebhookMessage;
import com.bkeuty.order.dto.shipping.ShippingOrderDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.RefundOrder;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.RefundStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.RefundOrderRepository;
import com.bkeuty.order.service.admin.AdminRefundOrderService;
import com.bkeuty.order.service.membership.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RefundOrderRepository refundOrderRepository;

    @Mock
    private MembershipService membershipService;

    @Mock
    private KafkaTemplate<String, CreateShippingOrderMessage> kafkaCreateShippingOrderTemplate;

    @Mock
    private KafkaTemplate<String, Object> kafkaEventTemplate;

    @Mock
    private AdminRefundOrderService adminRefundOrderService;

    private KafkaService kafkaService;

    @BeforeEach
    void setUp() {
        kafkaService = new KafkaService(
                orderRepository,
                orderItemRepository,
                membershipService,
                kafkaCreateShippingOrderTemplate,
                kafkaEventTemplate,
                adminRefundOrderService,
                refundOrderRepository
        );
    }

    @Test
    void listenToCreateRefundShippingOrderTopic_ShouldUpdateShippingCodeAndStatus_WhenRefundOrderExists() {
        CreateShippingResponseMessage message = new CreateShippingResponseMessage();
        message.setOrderId(100);
        
        CreateShippingOrderResponseDto responseDto = new CreateShippingOrderResponseDto();
        ShippingOrderDto data = new ShippingOrderDto();
        data.setOrderCode("GHN-REFUND-111");
        responseDto.setData(data);
        message.setShippingResponse(responseDto);

        RefundOrder mockRefund = RefundOrder.builder()
                .id(100)
                .status(RefundStatus.APPROVED)
                .build();

        when(refundOrderRepository.findById(100)).thenReturn(Optional.of(mockRefund));

        kafkaService.listenToCreateRefundShippingOrderTopic(message);

        assertEquals("GHN-REFUND-111", mockRefund.getShippingCode());
        assertEquals("picking", mockRefund.getShippingStatus());
        verify(refundOrderRepository, times(1)).save(mockRefund);
    }

    @Test
    void listenToUpdateShippingStatusTopic_ShouldUpdateRefundStatusToDelivered_WhenRefundMessageIsDelivered() {
        GhnWebhookMessage message = new GhnWebhookMessage();
        message.setRefund(true);
        message.setOrderCode("GHN-REFUND-111");
        message.setStatus("delivered");

        RefundOrder mockRefund = RefundOrder.builder()
                .id(100)
                .shippingCode("GHN-REFUND-111")
                .status(RefundStatus.APPROVED)
                .build();

        when(refundOrderRepository.findByShippingCode("GHN-REFUND-111")).thenReturn(mockRefund);

        kafkaService.listenToUpdateShippingStatusTopic(message);

        assertEquals("delivered", mockRefund.getShippingStatus());
        assertEquals(RefundStatus.DELIVERED, mockRefund.getStatus());
        verify(refundOrderRepository, times(1)).saveAndFlush(mockRefund);
    }

    @Test
    void listenToUpdateShippingStatusTopic_ShouldUpdateOrderStatusToSucceededAndRecalculateMembership_WhenOrderMessageIsDelivered() {
        GhnWebhookMessage message = new GhnWebhookMessage();
        message.setRefund(false);
        message.setOrderCode("GHN-ORDER-222");
        message.setStatus("delivered");

        Order mockOrder = Order.builder()
                .id(222)
                .userId("user-123")
                .shippingCode("GHN-ORDER-222")
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findByShippingCode("GHN-ORDER-222")).thenReturn(mockOrder);

        kafkaService.listenToUpdateShippingStatusTopic(message);

        assertEquals("delivered", mockOrder.getShippingStatus());
        assertEquals(OrderStatus.SUCCEEDED, mockOrder.getStatus());
        assertNotNull(mockOrder.getDeliveryDate());
        verify(orderRepository, times(1)).saveAndFlush(mockOrder);
        verify(membershipService, times(1)).recalculateMembershipLevel("user-123");
    }

    @Test
    void listenToRefundWalletSuccessTopic_ShouldMarkRefunded_WhenMessageIsValid() {
        RefundWalletSuccessEventDto message = RefundWalletSuccessEventDto.builder()
                .refundOrderId(100)
                .userId("user-123")
                .build();

        kafkaService.listenToRefundWalletSuccessTopic(message);

        verify(adminRefundOrderService, times(1)).markRefunded(100);
    }
}
