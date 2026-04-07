package com.bkeuty.order.controller.shipping;

import com.bkeuty.order.dto.shipping.CalShippingFeeDto;
import com.bkeuty.order.dto.shipping.CalShippingFeeResponseDto;
import com.bkeuty.order.service.shipping.ShippingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {
    private final ShippingService shippingService;
    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping("/fee")
    public Mono<CalShippingFeeResponseDto> calShippingFee(@RequestBody CalShippingFeeDto calShippingFeeDto) {
        return shippingService.calShippingFee(calShippingFeeDto);
    }
}
