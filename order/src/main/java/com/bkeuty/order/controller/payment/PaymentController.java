package com.bkeuty.order.controller.payment;

import com.bkeuty.order.dto.payment.PaymentStatusDto;
import com.bkeuty.order.dto.payment.PaymentWebhookData;
import com.bkeuty.order.dto.payment.WebHookResponseDTO;
import com.bkeuty.order.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @Value("${sepay.api-key}")
    private String webhookApiKey;
//    @PostMapping("/webhook")
//    public ResponseEntity<?> sepayWebhook(@RequestHeader("Authorization") String authHeader ,@RequestBody PaymentWebhookData webhook){
//        System.out.println("webhook: Call to webhook");
//        System.out.println("Received Webhook: " + webhook);
////        Boolean successUpdatePayment = paymentService.updatePaymentTransaction(webhook);
////        if (successUpdatePayment) {
////            return ResponseEntity.status(201).body(new WebHookResponseDTO(true));
////        }
////        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        if(authHeader==null){
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//        if (authHeader != null && authHeader.startsWith("Apikey ")) {
//            String apiKey = authHeader.substring(7);
//            if (webhookApiKey.equals(apiKey)) {
//                Boolean successUpdatePayment = paymentService.updatePaymentTransaction(webhook);
//                if (successUpdatePayment) {
//                    return ResponseEntity.status(201).body(new WebHookResponseDTO(true));
//                }
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//    }
    @PostMapping("/payment-status")
    public ResponseEntity<?> getPaymentStatus( @RequestBody PaymentStatusDto request){
        Boolean isPaymentSuccess = paymentService.checkPaymentStatus(request);
        if (isPaymentSuccess) {
            return ResponseEntity.status(200).body(new WebHookResponseDTO(true));
        }
        return ResponseEntity.status(200).body(new WebHookResponseDTO(false));
    }
}
