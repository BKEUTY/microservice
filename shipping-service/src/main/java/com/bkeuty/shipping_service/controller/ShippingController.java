package com.bkeuty.shipping_service.controller;

import com.bkeuty.shipping_service.dto.CalShippingFeeDto;
import com.bkeuty.shipping_service.dto.CalShippingFeeResponseDto;
import com.bkeuty.shipping_service.dto.CalShippingTimeDto;
import com.bkeuty.shipping_service.dto.CalShippingTimeResponseDto;
import com.bkeuty.shipping_service.service.ShippingService;
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
    @PostMapping("/leadtime")
    public Mono<CalShippingTimeResponseDto> calShippingTime(@RequestBody CalShippingTimeDto calShippingTimeDto) {
        return shippingService.calShippingTime(calShippingTimeDto);
    }

//    @PostMapping("/status")
//    public Mono<CalShippingTimeResponseDto> calShippingTime(@RequestBody CalShippingTimeDto calShippingTimeDto) {
//        return shippingService.calShippingTime(calShippingTimeDto);
//    }
}
