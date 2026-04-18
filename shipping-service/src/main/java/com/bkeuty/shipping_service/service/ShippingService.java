package com.bkeuty.shipping_service.service;

import com.bkeuty.shipping_service.dto.*;
import com.bkeuty.shipping_service.microservicecommunication.GHNCommunication;
import jakarta.ws.rs.BadRequestException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service
public class ShippingService {
    private final GHNCommunication ghnCommunication;
    public ShippingService(GHNCommunication ghnCommunication) {
        this.ghnCommunication = ghnCommunication;
    }
    public Mono<CalShippingFeeResponseDto> calShippingFee(CalShippingFeeDto calShippingFeeDto) {
        return ghnCommunication.getCalShippingFee(calShippingFeeDto);
    }

    public Mono<CalShippingTimeResponseDto> calShippingTime(CalShippingTimeDto calShippingTimeDto) {
        return ghnCommunication.getCalShippingTime(calShippingTimeDto);
    }

    public Mono<CreateShippingOrderResponseDto> createShippingOrder(CreateShippingOrderDto createShippingOrderDto) {
        return ghnCommunication.createShippingOrder(createShippingOrderDto);
    }
//    public GetShippingOrderStatusResponseDto getShippingOrderStatus(GetShippingOrderStatusRequest getShippingOrderStatusRequest, TokenValidationResponseDto  tokenValidationResponseDto) {
//        try {
//            Order order = orderRepository.findByIdAndUserId(getShippingOrderStatusRequest.getOrderId(),tokenValidationResponseDto.getUserId());
//            if (order == null) {
//                throw new BadRequestException("Order Not Found");
//            }
//            GetShippingOrderStatusResponseDto  getShippingOrderStatusResponseDto = ghnCommunication.getShippingStatus(getShippingOrderStatusRequest.getOrderCode()).block();
//            if(getShippingOrderStatusResponseDto!=null && getShippingOrderStatusResponseDto.getStatus()!=null) {
//                if(!getShippingOrderStatusResponseDto.getStatus().equals(order.getShippingStatus())) {
//                    order.setShippingStatus(getShippingOrderStatusResponseDto.getStatus());
//                    orderRepository.save(order);
//                }
//            }
//            return getShippingOrderStatusResponseDto;
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            throw new BadRequestException(e.getMessage());
//        }
//    }
}
