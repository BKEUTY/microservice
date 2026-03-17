package com.bkeuty.order.service.payment;

import com.bkeuty.order.dto.payment.PaymentStatusDto;
import com.bkeuty.order.dto.payment.PaymentWebhookData;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.PaymentTransaction;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository  orderRepository;
    public PaymentService(PaymentTransactionRepository paymentTransactionRepository, OrderRepository orderRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
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
        order.setStatus(PaymentStatus.PAID);
        orderRepository.save(order);
        return true;

    }

    public Boolean checkPaymentStatus(PaymentStatusDto paymentStatusDto) {
        Order order = orderRepository.findById(paymentStatusDto.getOrderId()).orElse(null);
        if (order == null) {
            return false;
        }
        return order.getStatus() == PaymentStatus.PAID;
    }

}
