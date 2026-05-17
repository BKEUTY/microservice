package com.bkeuty.order.service;

import com.bkeuty.order.dto.order.DecreaseStockResponseDto;
import com.bkeuty.order.dto.order.DecreaseStockStatusDto;
import com.bkeuty.order.dto.order.OrderEventDto;
import com.bkeuty.order.dto.order.RefundWalletSuccessEventDto;
import com.bkeuty.order.dto.shipping.AddressDto;
import com.bkeuty.order.dto.shipping.CreateShippingOrderDto;
import com.bkeuty.order.dto.shipping.CreateShippingOrderMessage;
import com.bkeuty.order.dto.shipping.CreateShippingResponseMessage;
import com.bkeuty.order.dto.shipping.GhnWebhookMessage;
import com.bkeuty.order.dto.shipping.ShippingItemDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.service.admin.AdminRefundOrderService;
import com.bkeuty.order.service.membership.MembershipService;
import com.bkeuty.order.util.OrderAddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

@Service
@Transactional
@Slf4j
public class KafkaService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MembershipService membershipService;
    private final KafkaTemplate<String, CreateShippingOrderMessage> kafkaCreateShippingOrderTemplate;
    private final KafkaTemplate<String, Object> kafkaEventTemplate;
    private final AdminRefundOrderService adminRefundOrderService;

    public KafkaService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MembershipService membershipService,
                        KafkaTemplate<String, CreateShippingOrderMessage> kafkaCreateShippingOrderTemplate,
                        KafkaTemplate<String, Object> kafkaEventTemplate,
                        AdminRefundOrderService adminRefundOrderService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.membershipService = membershipService;
        this.kafkaCreateShippingOrderTemplate = kafkaCreateShippingOrderTemplate;
        this.kafkaEventTemplate = kafkaEventTemplate;
        this.adminRefundOrderService = adminRefundOrderService;
    }

    @KafkaListener(topics = "payment-transaction-topic")
    public void listenPaymentTransactionTopic(String message) {
        Order order = orderRepository.findById(Integer.valueOf(message)).orElse(null);
        if(order!=null){
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(Integer.valueOf(message));
            AddressDto addressDto = toAddressDto(order.getAddress());
            CreateShippingOrderDto createShippingOrderDto = CreateShippingOrderDto.builder()
                    .note(order.getBuyerNote())
                    .toAddress(addressDto.getAddress())
                    .toName(order.getBuyerName())
                    .toPhone(order.getBuyerNumber())
                    .toDistrictName(addressDto.getDistrict().getDistrictName())
                    .toProvinceName(addressDto.getProvince().getProvinceName())
                    .toWardName(addressDto.getWard().getWardName())
                    .items(orderItems != null ? orderItems.stream().map(this::toShippingItemDto).toList() : null)
                    .build();
            kafkaCreateShippingOrderTemplate.send("create-shipping-order-topic", CreateShippingOrderMessage.builder().createShippingOrderDto(createShippingOrderDto).orderId(Integer.valueOf(message)).build());
            
            // Notify promotion service to commit voucher
            if (order.getVoucherId() != null) {
                OrderEventDto event = new OrderEventDto(
                    order.getId(), order.getUserId(), order.getVoucherId(), "COMPLETED"
                );
                kafkaEventTemplate.send("order-completed-topic", event);
            }
        }
    }

    @KafkaListener(topics = "decrease-stock-status-topic")
    public void listenToDecreaseStockStatus(DecreaseStockStatusDto message) {
        Integer orderId = message.getOrderId();
        if (orderId == null) {
            log.error("DecreaseStockStatusMessage: orderId is null");
            return;
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("DecreaseStockStatusMessage: order is null");
            return;
        }
        if(message.getIsSuccess().equals(true)) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
        else  {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            List<DecreaseStockResponseDto> decreaseStockFailedItem = message.getFailDecreaseStockItems();
            for (DecreaseStockResponseDto item : decreaseStockFailedItem) {
                OrderItem orderItem = orderItemRepository.findByOrderIdAndProductVariantId(orderId, item.getProductVariantId());
                if (orderItem != null) {
                    orderItem.setDecreasedStockFailed(Boolean.TRUE);
                    orderItemRepository.save(orderItem);
                }
            }

            // Notify promotion service to refund voucher
            if (order.getVoucherId() != null) {
                OrderEventDto event = new OrderEventDto(
                    order.getId(), order.getUserId(), order.getVoucherId(), "FAILED"
                );
                kafkaEventTemplate.send("order-failed-topic", event);
            }
        }
    }

    @KafkaListener(topics = "create-shipping-response-topic")
    public void listenToCreateShippingOrderTopic(CreateShippingResponseMessage message) {
        Order order = orderRepository.findById(message.getOrderId()).orElse(null);
        if(order!=null){
            order.setShippingCode(message.getShippingResponse().getData().getOrderCode());
            orderRepository.save(order);
        } else {
            log.error("listenToCreateShippingOrderTopic: order is null");
        }
    }

    @KafkaListener(topics = "update-shipping-status-topic")
    public void listenToUpdateShippingStatusTopic(GhnWebhookMessage message) {
        Order order = orderRepository.findByShippingCode(message.getOrderCode());
        if(order !=null){
            OrderStatus oldStatus = order.getStatus();
            order.setShippingStatus(message.getStatus());

            if ("delivered".equalsIgnoreCase(message.getStatus()) && oldStatus != OrderStatus.SUCCEEDED) {
                order.setStatus(OrderStatus.SUCCEEDED);

                order.setDeliveryDate(
                        LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                );
                orderRepository.saveAndFlush(order);
                membershipService.recalculateMembershipLevel(order.getUserId());
            } else {
                orderRepository.saveAndFlush(order);
            }
        } else {
            log.error("listenToUpdateShippingStatusTopic: order with shipping code {} not found", message.getOrderCode());
        }
    }


    /**
     * Listens to acknowledgement events from User Service after a wallet credit completes.
     * Updates the matching {@link com.bkeuty.order.entity.RefundOrder} status to REFUNDED.
     */
    @KafkaListener(topics = "refund-wallet-success-topic")
    public void listenToRefundWalletSuccessTopic(RefundWalletSuccessEventDto message) {
        if (message == null || message.getRefundOrderId() == null) {
            log.error("listenToRefundWalletSuccessTopic: received null or incomplete message");
            return;
        }
        log.info("Received refund-wallet-success-topic for refundOrderId={}", message.getRefundOrderId());
        adminRefundOrderService.markRefunded(message.getRefundOrderId());
    }

    private ShippingItemDto toShippingItemDto(OrderItem dto) {
        return ShippingItemDto.builder()
                .name(dto.getProductVariantName())
                .quantity(dto.getQuantity())
                .price(dto.getProductVariantPrice().intValue())
                .build();
    }

    private AddressDto toAddressDto(String address) {
        return OrderAddressUtils.toAddressDto(address);
    }
}
