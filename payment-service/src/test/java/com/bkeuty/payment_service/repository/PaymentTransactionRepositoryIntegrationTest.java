package com.bkeuty.payment_service.repository;

import com.bkeuty.payment_service.entity.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaymentTransactionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Test
    void saveAndFindTransaction_ShouldPersistCorrectly() {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setGateway("VietinBank");
        tx.setAccountNumber("0987654321");
        tx.setAmountIn(new BigDecimal("150000"));
        tx.setCode("TX100");
        tx.setTransactionContent("Thanh toan don hang 5555");
        tx.setTransactionDate(LocalDateTime.now());
        tx.setCreatedAt(LocalDateTime.now());

        PaymentTransaction persisted = entityManager.persist(tx);
        entityManager.flush();

        assertNotNull(persisted.getId());

        Optional<PaymentTransaction> found = paymentTransactionRepository.findById(persisted.getId());
        assertTrue(found.isPresent());
        assertEquals("VietinBank", found.get().getGateway());
        assertEquals("TX100", found.get().getCode());
        assertEquals("0987654321", found.get().getAccountNumber());
        assertEquals(0, new BigDecimal("150000").compareTo(found.get().getAmountIn()));
    }
}
