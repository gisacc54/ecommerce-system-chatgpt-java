package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.PaymentRequest;
import com.ecommerce.ecommerce.dto.PaymentResponse;
import com.ecommerce.ecommerce.entity.Order;
import com.ecommerce.ecommerce.entity.Payment;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Validates and records a payment for a given order.
     */
    public PaymentResponse confirmPayment(PaymentRequest request) {

        // Step 1: Validate the order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

        // Step 2: Ensure order is still pending
        if (!"pending".equalsIgnoreCase(order.getStatusStr())) {
            throw new RuntimeException("Order is not pending. Current status: " + order.getStatus());
        }

        // Step 3: Validate payment amount (must match or exceed order total)
        BigDecimal orderTotal = order.getTotalAmount();
        if (request.getAmount().compareTo(orderTotal) < 0) {
            throw new RuntimeException("Insufficient payment amount. Order total is " + orderTotal + " TZS.");
        }

        // Step 4: Update order status to 'paid'
        order.setStatusStr("paid");
        orderRepository.save(order);

        // Step 5: Create a payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "unknown");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Step 6: Build and return response
        return new PaymentResponse(
                order.getId(),
                payment.getAmount(),
                order.getStatusStr(),
                payment.getPaidAt()
        );
    }
}