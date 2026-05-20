package com.bkeuty.user_service;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, Object> kafkaTemplate;

	@MockitoBean
	private NewTopic refundSuccessTopic;

	@Test
	void contextLoads() {
	}

}
