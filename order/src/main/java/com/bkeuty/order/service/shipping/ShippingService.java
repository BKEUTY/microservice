package com.bkeuty.order.service.shipping;

import com.bkeuty.order.dto.shipping.CalShippingFeeDto;
import com.bkeuty.order.dto.shipping.CalShippingFeeResponseDto;
import com.bkeuty.order.dto.shipping.GHNProvinceListResponse;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ShippingService {
    private final GHNCommunication ghnCommunication;
    ShippingService(GHNCommunication ghnCommunication) {
        this.ghnCommunication = ghnCommunication;
    }
    public Mono<CalShippingFeeResponseDto> calShippingFee(CalShippingFeeDto  calShippingFeeDto) {
        return ghnCommunication.getCalShippingFee(calShippingFeeDto);
    }
}
