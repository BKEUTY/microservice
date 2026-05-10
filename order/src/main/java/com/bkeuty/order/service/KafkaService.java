package com.bkeuty.order.service;

import com.bkeuty.order.dto.order.DecreaseStockResponseDto;
import com.bkeuty.order.dto.order.DecreaseStockStatusDto;
import com.bkeuty.order.dto.order.OrderEventDto;
import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    KafkaTemplate<String, CreateShippingOrderMessage> kafkaCreateShippingOrderTemplate;
    KafkaTemplate<String, Object> kafkaEventTemplate;
    Logger logger = Logger.getLogger(KafkaService.class);

    public KafkaService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,  KafkaTemplate<String, CreateShippingOrderMessage> kafkaCreateShippingOrderTemplate, KafkaTemplate<String, Object> kafkaEventTemplate) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.kafkaCreateShippingOrderTemplate = kafkaCreateShippingOrderTemplate;
        this.kafkaEventTemplate = kafkaEventTemplate;
    }
    @KafkaListener(topics = "payment-transaction-topic")
    public void listenPaymentTransactionTopic(String message){
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
                    .items(orderItems!=null?orderItems.stream().map(this::toShippingItemDto).toList():null)
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
            logger.error("DecreaseStockStatusMessage: orderId is null");
            return;
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            logger.error("DecreaseStockStatusMessage: order is null");
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
    public void listenToCreateShippingOrderTopic(CreateShippingResponseMessage message){
        Order order = orderRepository.findById(message.getOrderId()).orElse(null);
        if(order!=null){
            order.setShippingCode(message.getShippingResponse().getData().getOrderCode());
            orderRepository.save(order);
        }
        else {
            logger.error("listenToCreateShippingOrderTopic: order is null");
        }
    }
    @KafkaListener(topics = "update-shipping-status-topic")
    public void listenToUpdateShippingStatusTopic(GhnWebhookMessage message){
        Order order = orderRepository.findByShippingCode(message.getOrderCode());
        if(order !=null){
            order.setShippingStatus(message.getStatus());
            orderRepository.save(order);
        }
        else {
            logger.error("listenToUpdateShippingStatusTopic: order is null");
        }
    }
    private ShippingItemDto toShippingItemDto(OrderItem dto) {
        return ShippingItemDto.builder()
                .name(dto.getProductVariantName())
                .quantity(dto.getQuantity())
                .price(dto.getProductVariantPrice().intValue())
                .build();
    }
    private AddressDto toAddressDto(String address) {
        String[] addressArray = address.split("\\|");
        if(addressArray.length!=2){
            return null;
        }
        String nameField = addressArray[0];
        String codeField = addressArray[1];
        String[] nameArray = nameField.split(",\\s*");
        if(nameArray.length< 4){
            return null;
        }
        int nameLength = nameArray.length;

        StringBuilder addressName  = new StringBuilder();
        for(int nameIndex=0;nameIndex<nameLength-3;nameIndex++){
            addressName.append(", ").append(nameArray[nameIndex]);
        }

        String wardName  = nameArray[nameLength-3];
        String districtName = nameArray[nameLength-2];
        String provinceName = nameArray[nameLength-1];
        String[] codeArray = codeField.split(":");
        if(codeArray.length!=3){
            return null;
        }
        String wardCode = codeArray[0];
        String districtCode = codeArray[1];
        String provinceCode = codeArray[2];
        return AddressDto.builder()
                .address(addressName.toString())
                .ward(new WardDto(Integer.valueOf(wardCode), wardName))
                .district(new DistrictDto(Integer.valueOf(districtCode), districtName))
                .province(new ProvinceDto(Integer.valueOf(provinceCode), provinceName))
                .build();
    }
}
