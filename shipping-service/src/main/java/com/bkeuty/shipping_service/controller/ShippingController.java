package com.bkeuty.shipping_service.controller;

import com.bkeuty.shipping_service.dto.*;
import com.bkeuty.shipping_service.service.KafkaService;
import com.bkeuty.shipping_service.service.ShippingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {
    private final ShippingService shippingService;
    private final KafkaService kafkaService;
    public ShippingController(ShippingService shippingService, KafkaService kafkaService) {
        this.shippingService = shippingService;
        this.kafkaService = kafkaService;
    }
    @GetMapping("healthcheck")
    public ResponseEntity<String> getHealthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/fee")
    public Mono<CalShippingFeeResponseDto> calShippingFee(@RequestBody CalShippingFeeDto calShippingFeeDto) {
        return shippingService.calShippingFee(calShippingFeeDto);
    }
    @PostMapping("/leadtime")
    public Mono<CalShippingTimeResponseDto> calShippingTime(@RequestBody CalShippingTimeDto calShippingTimeDto) {
        return shippingService.calShippingTime(calShippingTimeDto);
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> shippingWebhook(GhnWebhookDto  ghnWebhookDto) {
        kafkaService.sendUpdateShippingOrder(ghnWebhookDto);
        return ResponseEntity.ok().build();
    }


//    @PostMapping("/status")
//    public Mono<CalShippingTimeResponseDto> calShippingTime(@RequestBody CalShippingTimeDto calShippingTimeDto) {
//        return shippingService.calShippingTime(calShippingTimeDto);
//    }
}
