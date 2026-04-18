package com.bkeuty.payment_service.controller;

import com.bkeuty.payment_service.dto.PaymentStatusDto;
import com.bkeuty.payment_service.dto.PaymentWebhookData;
import com.bkeuty.payment_service.dto.WebHookResponseDTO;
import com.bkeuty.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @Value("${sepay.api-key}")
    private String webhookApiKey;
    @PostMapping("/webhook")
    public ResponseEntity<?> sepayWebhook(@RequestHeader("Authorization") String authHeader , @RequestBody PaymentWebhookData webhook){
        System.out.println("webhook: Call to webhook");
        System.out.println("Received Webhook: " + webhook);
//        Boolean successUpdatePayment = paymentService.updatePaymentTransaction(webhook);
//        if (successUpdatePayment) {
//            return ResponseEntity.status(201).body(new WebHookResponseDTO(true));
//        }
//        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if(authHeader==null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (authHeader != null && authHeader.startsWith("Apikey ")) {
            String apiKey = authHeader.substring(7);
            if (webhookApiKey.equals(apiKey)) {
                Boolean successUpdatePayment = paymentService.updatePaymentTransaction(webhook);
                if (successUpdatePayment) {
                    return ResponseEntity.status(201).body(new WebHookResponseDTO(true));
                }
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
//    @PostMapping("/status")
//    public ResponseEntity<?> getPaymentStatus( @RequestBody PaymentStatusDto request){
////        Boolean isPaymentSuccess = paymentService.checkPaymentStatus(request);
//        if (isPaymentSuccess) {
//            return ResponseEntity.status(200).body(new WebHookResponseDTO(true));
//        }
//        return ResponseEntity.status(200).body(new WebHookResponseDTO(false));
//    }
}
