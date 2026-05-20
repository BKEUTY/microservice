package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.refund.ProcessRefundEventDto;
import com.bkeuty.user_service.dto.refund.RefundWalletSuccessEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private NewTopic refundSuccessTopic;

    private KafkaService kafkaService;

    @BeforeEach
    void setUp() {
        kafkaService = new KafkaService(userService, kafkaTemplate, refundSuccessTopic);
    }

    @Test
    void processRefundTopicListener_ShouldUpdateUserWalletAndPublishSuccessEvent() {
        ProcessRefundEventDto eventDto = ProcessRefundEventDto.builder()
                .refundOrderId(100)
                .orderId(1)
                .userId("user-123")
                .amount(new BigDecimal("150000"))
                .build();

        kafkaService.processRefundTopicListener(eventDto);

        verify(userService, times(1)).updateUserWallet("user-123", new BigDecimal("150000"));

        ArgumentCaptor<RefundWalletSuccessEventDto> captor = ArgumentCaptor.forClass(RefundWalletSuccessEventDto.class);
        verify(kafkaTemplate, times(1)).send(eq("refund-wallet-success-topic"), captor.capture());

        RefundWalletSuccessEventDto sent = captor.getValue();
        assertEquals(100, sent.getRefundOrderId());
        assertEquals("user-123", sent.getUserId());
    }
}
