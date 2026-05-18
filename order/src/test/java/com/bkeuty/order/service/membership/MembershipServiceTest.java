package com.bkeuty.order.service.membership;

import com.bkeuty.order.dto.internal.UserDetailResponseDto;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WebClient userWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;
    @Mock
    private WebClient.RequestBodySpec requestBodySpecMock;
    @Mock
    private WebClient.ResponseSpec responseSpecMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;
    @Mock
    private Mono<UserDetailResponseDto> monoMock;
    @Mock
    private Mono<ResponseEntity<Void>> monoVoidMock;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void recalculateMembershipLevel_ShouldUpdateLevel_WhenSpendingIsValid() {
        String userId = "user123";
        BigDecimal totalSpending = new BigDecimal("5000000"); // E.g., level 2

        when(orderRepository.sumTotalSpendingByUserId(userId, List.of(OrderStatus.SUCCEEDED)))
                .thenReturn(totalSpending);

        when(userWebClient.patch()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString(), eq(userId), anyInt(), eq(totalSpending))).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toBodilessEntity()).thenReturn(monoVoidMock);
        when(monoVoidMock.block()).thenReturn(ResponseEntity.ok().build());

        membershipService.recalculateMembershipLevel(userId);

        verify(orderRepository, times(1)).sumTotalSpendingByUserId(userId, List.of(OrderStatus.SUCCEEDED));
        verify(userWebClient, times(1)).patch();
    }

    @Test
    void getMembershipLevel_ShouldReturnLevel_WhenUserExists() {
        String userId = "user123";
        UserDetailResponseDto dto = new UserDetailResponseDto();
        dto.setMembershipLevel(3);

        when(userWebClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri("/api/user/internal/{userId}", userId)).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(UserDetailResponseDto.class)).thenReturn(monoMock);
        when(monoMock.block()).thenReturn(dto);

        int level = membershipService.getMembershipLevel(userId);

        assertEquals(3, level);
        verify(userWebClient, times(1)).get();
    }

    @Test
    void getMembershipLevel_ShouldReturnZero_WhenExceptionOccurs() {
        String userId = "user123";

        when(userWebClient.get()).thenThrow(new RuntimeException("Connection Refused"));

        int level = membershipService.getMembershipLevel(userId);

        assertEquals(0, level);
    }
}
