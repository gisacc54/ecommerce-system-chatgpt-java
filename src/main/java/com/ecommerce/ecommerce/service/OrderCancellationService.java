// src/main/java/com/ecommerce/ecommerce/service/OrderCancellationService.java
package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.CancelOrderResponse;
import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.ecommerce.exception.RefundFailedException;
import com.ecommerce.ecommerce.repository.*;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class OrderCancellationService {

    private final Logger log = LoggerFactory.getLogger(OrderCancellationService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final AuditRepository auditRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate; // used to call external payment gateway

    public OrderCancellationService(OrderRepository orderRepository,
                                    OrderItemRepository orderItemRepository,
                                    ProductRepository productRepository,
                                    PaymentRepository paymentRepository,
                                    AuditRepository auditRepository,
                                    JavaMailSender mailSender,
                                    RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.auditRepository = auditRepository;
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
    }

    /**
     * Cancel an order and (if paid) process refund, adjust inventory, send emails, and log audit.
     * Everything that modifies DB is wrapped in a single transactional boundary.
     */
    @Transactional
    public CancelOrderResponse cancelOrderWithRefund(Long orderId, Long performedByUserId) {
        // 1) Fetch order with items and user
        Order order = orderRepository.findByIdWithItemsAndUser(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2) If already cancelled, simply return info (idempotent)
        if (order.getStatus() == Order.Status.CANCELLED) {
            log.info("Order {} already cancelled", orderId);
            return buildResponse(order, BigDecimal.ZERO);
        }

        BigDecimal refundAmount = BigDecimal.ZERO;

        // 3) If order was PAID, locate payment and initiate refund via external API
        if (order.getStatus() == Order.Status.PAID) {
            // Try to find a successful payment for this order
            Optional<Payment> maybePayment = paymentRepository.findFirstByOrderIdAndStatus(orderId, Payment.Status.COMPLETED);
            if (maybePayment.isPresent()) {
                Payment payment = maybePayment.get();

                // Determine refund amount: for simplicity refund full order total (can be partial)
                refundAmount = order.getTotalAmount();

                // Call payment gateway API to issue refund (this is a placeholder example)
                try {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("paymentId", payment.getId());
                    payload.put("amount", refundAmount);
                    payload.put("currency", "TZS");
                    payload.put("reason", "Order cancellation: " + orderId);

                    // Example: POST https://payments.example.com/api/refunds
                    // For production use: auth headers, timeouts, retry, secure storage of API keys
                    String gatewayUrl = "https://payment-gateway.example.com/api/refunds";


                    // Optionally record refund in Payment entity or a Refund table
                    Payment refundRecord = new Payment();
                    refundRecord.setOrder(order);
                    refundRecord.setAmount(refundAmount.negate()); // negative for refund
                    refundRecord.setStatus(Payment.Status.REFUNDED);
                    refundRecord.setCreatedAt(LocalDateTime.now());
                    paymentRepository.save(refundRecord);

                } catch (RestClientException ex) {
                    log.error("Refund request failed for order {}: {}", orderId, ex.getMessage(), ex);
                    throw new RefundFailedException("CAPTURED: refund failed due to payment gateway error");
                }
            } else {
                // No recorded payment but status is PAID — log and set refund 0
                log.warn("Order {} marked PAID but no completed payment record found", orderId);
            }
        }

        // 4) Adjust inventory for all order items (batch update). We lock each product pessimistically to be safe.
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : items) {
            Long productId = item.getProduct().getId();

            // fetch product with PESSIMISTIC_WRITE lock to avoid concurrent stock updates
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new IllegalStateException("Product not found when adjusting inventory: " + productId));

            // increase stock by cancelled quantity (returns items back to inventory)
            int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            int newStock = currentStock + item.getQuantity();
            product.setStockQuantity(newStock);

            // persist product stock change (will be flushed at transaction commit)
            productRepository.save(product);
        }

        // 5) Update order status to CANCELLED and save meta (confirmationSent etc)
        order.setStatus(Order.Status.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 6) Send notification emails to user and admin (best-effort; don't break transaction on email failure)
        try {
            // Email to user
            SimpleMailMessage userMail = new SimpleMailMessage();
            userMail.setTo(order.getUser().getEmail());
            userMail.setSubject("Order cancelled: " + order.getId());
            userMail.setText(String.format("Hello %s,\n\nYour order %d has been cancelled. Refund amount: %s TZS.\n\nAsante, TZMart",
                    order.getUser().getName(),
                    order.getId(),
                    formatTzs(refundAmount)));
            mailSender.send(userMail);

            // Email to admin
            SimpleMailMessage adminMail = new SimpleMailMessage();
            adminMail.setTo("admin@yourshop.tz"); // replace with config
            adminMail.setSubject("Order cancelled (admin): " + order.getId());
            adminMail.setText(String.format("Order %d cancelled by system/user. Refund: %s TZS. Items: %d",
                    order.getId(), formatTzs(refundAmount), items.size()));
            mailSender.send(adminMail);
        } catch (Exception ex) {
            // Log but do not rollback transaction on failure to send email
            log.error("Failed to send cancellation emails for order {}: {}", order.getId(), ex.getMessage());
        }

        // 7) Write audit trail (who cancelled, when, reason)
        AuditLog audit = new AuditLog();
        audit.setAction("ORDER_CANCELLED");
        audit.setEntityId(order.getId());
        audit.setPerformedBy(performedByUserId);
        audit.setPerformedAt(LocalDateTime.now());
        audit.setDetails(String.format("Order %d cancelled; refund= %s TZS", order.getId(), formatTzs(refundAmount)));
        auditRepository.save(audit);

        // 8) Build response DTO
        return buildResponse(order, refundAmount);
    }

    private CancelOrderResponse buildResponse(Order order, BigDecimal refundAmount) {
        CancelOrderResponse resp = new CancelOrderResponse();
        resp.setOrderId(order.getId());
        resp.setStatus(order.getStatusStr()); // e.g., 'cancelled'
        resp.setRefundAmount(refundAmount);

        List<CancelOrderResponse.OrderItemDto> itemDtos = order.getOrderItems().stream().map(oi -> {
            CancelOrderResponse.OrderItemDto dto = new CancelOrderResponse.OrderItemDto();
            dto.setProductId(oi.getProduct().getId());
            dto.setProductName(oi.getProduct().getName());
            dto.setQuantity(oi.getQuantity());
            dto.setUnitPriceTZS(formatTzs(oi.getPrice()));
            return dto;
        }).collect(Collectors.toList());

        resp.setItems(itemDtos);
        return resp;
    }

    // format amount in TZS with grouping separators — e.g., 150,000 TZS
    private String formatTzs(BigDecimal amount) {
        if (amount == null) return "0";
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("en", "TZ")); // locale for formatting only
        return nf.format(amount);
    }
}