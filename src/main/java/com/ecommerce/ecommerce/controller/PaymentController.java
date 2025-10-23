package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.PaymentRequest;
import com.ecommerce.ecommerce.dto.PaymentResponse;
import com.ecommerce.ecommerce.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // Constructor-based dependency injection
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint: POST /payments/confirm
     * Confirms a payment for a given order and marks it as 'paid'.
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}