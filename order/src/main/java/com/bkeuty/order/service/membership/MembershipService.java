package com.bkeuty.order.service.membership;

import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.util.MembershipLevelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class MembershipService {
    private final OrderRepository orderRepository;
    private final WebClient userWebClient;

    public MembershipService(OrderRepository orderRepository, WebClient userWebClient) {
        this.orderRepository = orderRepository;
        this.userWebClient = userWebClient;
    }

    public void recalculateMembershipLevel(String userId) {
        try {
            BigDecimal totalSpending = orderRepository.sumTotalSpendingByUserId(userId, List.of(OrderStatus.SUCCEEDED));
            
            int newLevel = MembershipLevelUtils.calculateLevel(totalSpending);
            
            userWebClient.patch()
                    .uri("/api/user/internal/{userId}/membership-level?level={level}&totalSpending={totalSpending}", userId, newLevel, totalSpending)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to recalculate/update membership for user {}: {}", userId, e.getMessage(), e);
        }
    }

    public int getMembershipLevel(String userId) {
        try {
            com.bkeuty.order.dto.internal.UserDetailResponseDto userDetail = userWebClient.get()
                    .uri("/api/user/internal/{userId}", userId)
                    .retrieve()
                    .bodyToMono(com.bkeuty.order.dto.internal.UserDetailResponseDto.class)
                    .block();
            return userDetail != null && userDetail.getMembershipLevel() != null ? userDetail.getMembershipLevel() : 0;
        } catch (Exception e) {
            log.error("Failed to fetch membership level for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }
}
