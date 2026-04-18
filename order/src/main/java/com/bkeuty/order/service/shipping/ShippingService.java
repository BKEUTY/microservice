package com.bkeuty.order.service.shipping;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import com.bkeuty.order.repository.OrderRepository;
import jakarta.ws.rs.BadRequestException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
public class ShippingService {
    private final GHNCommunication ghnCommunication;
    public final OrderRepository orderRepository;
    ShippingService(GHNCommunication ghnCommunication,  OrderRepository orderRepository) {
        this.ghnCommunication = ghnCommunication;
        this.orderRepository = orderRepository;
    }
    public Mono<CalShippingFeeResponseDto> calShippingFee(CalShippingFeeDto  calShippingFeeDto) {
        return ghnCommunication.getCalShippingFee(calShippingFeeDto);
    }

    public Mono<CalShippingTimeResponseDto> calShippingTime(CalShippingTimeDto calShippingTimeDto) {
        return ghnCommunication.getCalShippingTime(calShippingTimeDto);
    }

    public Mono<CreateShippingOrderResponseDto> createShippingOrder(CreateShippingOrderDto createShippingOrderDto) {
        return ghnCommunication.createShippingOrder(createShippingOrderDto);
    }
    public GetShippingOrderStatusResponseDto getShippingOrderStatus(GetShippingOrderStatusRequest getShippingOrderStatusRequest, TokenValidationResponseDto  tokenValidationResponseDto) {
        try {
            Order order = orderRepository.findByIdAndUserId(getShippingOrderStatusRequest.getOrderId(),tokenValidationResponseDto.getUserId());
            if (order == null) {
                throw new BadRequestException("Order Not Found");
            }
            GetShippingOrderStatusResponseDto  getShippingOrderStatusResponseDto = ghnCommunication.getShippingStatus(getShippingOrderStatusRequest.getOrderCode()).block();
//            if(getShippingOrderStatusResponseDto!=null && getShippingOrderStatusResponseDto.getStatus()!=null) {
//                    order.setShippingStatus(getShippingOrderStatusResponseDto.getStatus());
//                    orderRepository.save(order);
//            }
            return getShippingOrderStatusResponseDto;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
}
