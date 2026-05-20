package com.bkeuty.payment_service.service;

import com.bkeuty.payment_service.dto.PaymentWebhookData;
import com.bkeuty.payment_service.entity.PaymentTransaction;
import com.bkeuty.payment_service.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void updatePaymentTransaction_ShouldSaveTransactionAndPublishToKafka() {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(999);
        webhookData.setContent("Thanh toan don hang DH12345");
        webhookData.setAccountNumber("0123456789");
        webhookData.setDescription("Payment for order 12345");
        webhookData.setTransferAmount(new BigDecimal("500000"));
        webhookData.setAccumulated(new BigDecimal("500000"));
        webhookData.setGateway("VietinBank");
        webhookData.setSubAccount("sub001");
        webhookData.setReferenceCode("REF001");
        webhookData.setTransactionDate(LocalDateTime.now());

        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Boolean result = paymentService.updatePaymentTransaction(webhookData);

        assertTrue(result);

        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionRepository, times(1)).save(captor.capture());

        PaymentTransaction saved = captor.getValue();
        assertEquals("0123456789", saved.getAccountNumber());
        assertEquals("999", saved.getCode());
        assertEquals("VietinBank", saved.getGateway());

        verify(kafkaTemplate, times(1)).send(eq("payment-transaction-topic"), eq("12345"));
    }

    @Test
    void updatePaymentTransaction_ShouldExtractCorrectOrderId_FromMixedContent() {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(1);
        webhookData.setContent("DH999 - Thanh toan");
        webhookData.setTransactionDate(LocalDateTime.now());

        when(paymentTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.updatePaymentTransaction(webhookData);

        verify(kafkaTemplate, times(1)).send(eq("payment-transaction-topic"), eq("999"));
    }

    @Test
    void updatePaymentTransaction_ShouldMapAllFields_Correctly() {
        LocalDateTime txDate = LocalDateTime.of(2024, 5, 1, 10, 0, 0);

        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(77);
        webhookData.setContent("DH777 - Order");
        webhookData.setAccountNumber("ACC001");
        webhookData.setDescription("Order description");
        webhookData.setTransferAmount(new BigDecimal("200000"));
        webhookData.setAccumulated(new BigDecimal("200000"));
        webhookData.setGateway("BIDV");
        webhookData.setSubAccount("sub_bidv");
        webhookData.setReferenceCode("REFBIDV");
        webhookData.setTransactionDate(txDate);

        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        paymentService.updatePaymentTransaction(webhookData);

        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionRepository).save(captor.capture());

        PaymentTransaction tx = captor.getValue();
        assertEquals("77", tx.getCode());
        assertEquals("Order description", tx.getBody());
        assertEquals("DH777 - Order", tx.getTransactionContent());
        assertEquals("BIDV", tx.getGateway());
        assertEquals("sub_bidv", tx.getSubAccount());
        assertEquals("REFBIDV", tx.getReferenceNumber());
        assertEquals(txDate, tx.getCreatedAt());
    }

    @Test
    void updatePaymentTransaction_ShouldThrowNullPointerException_WhenContentContainsNoDHPattern() {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(10);
        webhookData.setContent("Thanh toan don hang khong co so");
        webhookData.setAccountNumber("0123456789");
        webhookData.setTransactionDate(LocalDateTime.now());

        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertThrows(NullPointerException.class, () -> {
            paymentService.updatePaymentTransaction(webhookData);
        });
    }
}
