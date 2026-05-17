package com.bkeuty.payment_service.service;

import com.bkeuty.payment_service.dto.PaymentWebhookData;
import com.bkeuty.payment_service.entity.PaymentTransaction;
import com.bkeuty.payment_service.repository.PaymentTransactionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    public PaymentService(PaymentTransactionRepository paymentTransactionRepository,  KafkaTemplate<String,String> kafkaTemplate) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    public  Integer extractOrderNumber(String text) {
        Pattern pattern = Pattern.compile("DH(\\d+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }

        return null;
    }
    public Boolean updatePaymentTransaction(PaymentWebhookData paymentWebhookData) {
        String content = paymentWebhookData.getContent();

        Integer orderId = extractOrderNumber(content);
        System.out.println("orderId:" + orderId);

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
        kafkaTemplate.send("payment-transaction-topic", orderId.toString());
        return true;
    }
}
