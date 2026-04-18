package com.bkeuty.order.service.payment;

import com.bkeuty.order.dto.payment.PaymentStatusDto;
import com.bkeuty.order.dto.payment.PaymentWebhookData;
import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.entity.PaymentTransaction;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.PaymentTransactionRepository;
import com.bkeuty.order.service.shipping.ShippingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository  orderRepository;
    private final ShippingService shippingService;
    private final OrderItemRepository orderItemRepository;
    public PaymentService(PaymentTransactionRepository paymentTransactionRepository, OrderRepository orderRepository, ShippingService shippingService,  OrderItemRepository orderItemRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.shippingService = shippingService;
        this.orderItemRepository = orderItemRepository;
    }
    public Boolean updatePaymentTransaction(PaymentWebhookData paymentWebhookData) {
        String content = paymentWebhookData.getContent();
        Integer orderId =  Integer.valueOf(content.replaceAll("\\D", ""));
        System.out.println("orderId:"+orderId);
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAccountNumber(paymentWebhookData.getAccountNumber());
        paymentTransaction.setCode(paymentWebhookData.getId().toString());
        paymentTransaction.setBody(paymentWebhookData.getDescription());
        paymentTransaction.setTransactionDate(paymentWebhookData.getTransactionDate());
        paymentTransaction.setAccumulated(paymentWebhookData.getAccumulated());
        paymentTransaction.setTransactionContent(paymentWebhookData.getContent());
        paymentTransaction.setGateway(paymentWebhookData.getGateway());
        paymentTransaction.setAmountIn(paymentWebhookData.getTransferAmount());
        paymentTransaction.setSubAccount(paymentWebhookData.getSubAccount());
        paymentTransaction.setReferenceNumber(paymentWebhookData.getReferenceCode());
        paymentTransaction.setCreatedAt(paymentWebhookData.getTransactionDate());
        paymentTransactionRepository.save(paymentTransaction);
        order.setPaymentStatus(PaymentStatus.PAID);
        AddressDto addressDto = toAddressDto(order.getAddress());
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        CreateShippingOrderResponseDto createShippingOrderResponseDto =shippingService.createShippingOrder(CreateShippingOrderDto.builder()
                .note(order.getBuyerNote())
                .toAddress(addressDto.getAddress())
                .toName(order.getBuyerName())
                .toPhone(order.getBuyerNumber())
                .toDistrictName(addressDto.getDistrict().getDistrictName())
                .toProvinceName(addressDto.getProvince().getProvinceName())
                .toWardName(addressDto.getWard().getWardName())
                        .items(orderItems!=null?orderItems.stream().map(this::toShippingItemDto).toList():null)
                .build()).block();
//        order.setShippingCode(createShippingOrderResponseDto.getData().getOrderCode());
        order.setEstimatedShippingDate(createShippingOrderResponseDto.getData().getExpectedDeliveryTime());
        orderRepository.save(order);
        return true;

    }

    public Boolean checkPaymentStatus(PaymentStatusDto paymentStatusDto) {
        Order order = orderRepository.findById(paymentStatusDto.getOrderId()).orElse(null);
        if (order == null) {
            return false;
        }
        return order.getPaymentStatus()== PaymentStatus.PAID;
    }
    private ShippingItemDto toShippingItemDto(OrderItem dto) {
        return ShippingItemDto.builder()
                .name(dto.getProductVariantName())
                .quantity(dto.getQuantity())
                .price(dto.getProductVariantPrice().intValue())
                .build();
    }
    private AddressDto toAddressDto(String address) {
        AddressDto addressDto = new AddressDto();
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
