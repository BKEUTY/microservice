package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.refund.ProcessRefundEventDto;
import com.bkeuty.user_service.dto.refund.RefundWalletSuccessEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic refundSuccessTopic;

    public KafkaService(UserService userService, KafkaTemplate<String, Object> kafkaTemplate, NewTopic refundSuccessTopic) {
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
        this.refundSuccessTopic = refundSuccessTopic;
    }

    @KafkaListener(topics = "process-refund-topic")
    public void processRefundTopicListener(ProcessRefundEventDto processRefundEventDto) {
        userService.updateUserWallet(processRefundEventDto.getUserId(), processRefundEventDto.getAmount().negate());
        kafkaTemplate.send("refund-wallet-success-topic", RefundWalletSuccessEventDto.builder().userId(processRefundEventDto.getUserId()).refundOrderId(processRefundEventDto.getRefundOrderId()).build());

    }
}
