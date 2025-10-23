package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final JavaMailSender mailSender;

    public OrderService(CartRepository cartRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository, PaymentRepository paymentRepository, JavaMailSender mailSender) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.mailSender = mailSender;
    }

    /**
     * Places a new order for the authenticated user.
     */
    @Transactional
    public PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        // 1️⃣ Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // 2️⃣ Fetch user's cart items
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty. Add products before placing an order.");
        }

        // 3️⃣ Calculate total order amount in TZS
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found."));

            // Check stock availability
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Subtotal = price × quantity
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        // 4️⃣ Create order record
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(request.getShippingAddress());
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 5️⃣ Create order items for each cart entry
        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItemRepository.save(orderItem);

            // 6️⃣ Deduct stock from inventory
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 7️⃣ Clear user's cart after order placement
        cartRepository.deleteAll(cartItems);

        // 8️⃣ Return order summary response
        return new PlaceOrderResponse(order.getId(), totalAmount, order.getStatusStr());
    }

    /**
     * Cancel an order and optionally process refund
     *
     * @param orderId the ID of the order to cancel
     * @return CancelOrderResponse containing status and refund info
     */
    @Transactional
    public CancelOrderResponse cancelOrder(Long orderId) {
        // 1. Fetch the order by ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // 2. Validate current order status
        if ("cancelled".equalsIgnoreCase(order.getStatusStr())) {
            throw new RuntimeException("Order is already cancelled.");
        }
        if ("completed".equalsIgnoreCase(order.getStatusStr())) {
            throw new RuntimeException("Completed orders cannot be cancelled.");
        }

        // 3. Update order status to 'cancelled'
        order.setStatus("cancelled");
        orderRepository.save(order);

        boolean refundProcessed = false;

        // 4. Optional: process refund if payment exists
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null && "paid".equalsIgnoreCase(payment.getOrder().getStatusStr())) {
            // Here you could integrate with payment gateway for refund
            // For now, mark refund as processed in database
            payment.setRefunded(true);
            payment.setRefundedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            refundProcessed = true;
        }

        // 5. Build response
        CancelOrderResponse response = new CancelOrderResponse();
        response.setOrderId(orderId);
        response.setStatus("cancelled");

        return response;
    }
    /**
     * Fetch past orders for a given user ID.
     */
    public List<OrderHistoryResponse> getUserOrderHistory(Long userId) {
        // 1️⃣ Check if the user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        // 2️⃣ Retrieve all orders for the user
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 3️⃣ Map entity list to DTOs for response
        return orders.stream()
                .map(order -> new OrderHistoryResponse(
                        order.getId(),
                        order.getStatusStr(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


    /**
     * Sends order confirmation email for the given order id.
     * Fetches order and items, constructs email, sends it and updates the order.confirmationSent flag.
     *
     * @param orderId id of the order to confirm
     * @return OrderConfirmationResponse with status and timestamp
     * @throws IllegalArgumentException if order not found
     * @throws MailException or MessagingException on email sending errors
     */
    @Transactional
    public OrderConfirmationResponse sendOrderConfirmationEmail(Long orderId) throws MessagingException {
        // 1) Fetch order with items + user
        Order order = orderRepository.findByIdWithItemsAndUser(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 2) Map order items to DTOs
        List<OrderItemDto> items = order.getOrderItems()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // 3) Build email subject & body (simple HTML)
        String subject = String.format("Order Confirmation — Order #%d", order.getId());
        String body = buildEmailBody(order, items);

        // 4) Get recipient email (from associated user)
        User user = order.getUser();
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("Order has no associated user email");
        }
        String to = user.getEmail();

        // 5) Send email using JavaMailSender (MimeMessage with HTML)
        try {
            sendHtmlEmail(to, subject, body);
        } catch (MailException | MessagingException e) {
            // rethrow so controller can return 400, but we do NOT update confirmationSent on failure
            throw e;
        }

        // 6) Update confirmationSent field and persist (transactional ensures commit)
        order.setConfirmationSent(true);
        order.setConfirmationSentAt(LocalDateTime.now()); // add this field to Order if available
        orderRepository.save(order);

        // 7) Build response DTO
        return new OrderConfirmationResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatusStr(),
                true,
                "Confirmation email sent successfully",
                order.getConfirmationSentAt()
        );
    }

    private OrderItemDto mapToDto(OrderItem oi) {
        return new OrderItemDto(
                oi.getId(),                 // orderItem ID
                oi.getProduct().getId(),    // product ID
                oi.getProduct().getName(),  // product name
                oi.getQuantity(),
                oi.getPrice()
        );
    }

    private String buildEmailBody(Order order, List<OrderItemDto> items) {
        // Simple HTML email body. You can use a templating engine (Thymeleaf) for complex templates.
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Asante — Order Confirmation (Thank you)</h2>");
        sb.append("<p>Order ID: ").append(order.getId()).append("</p>");
        sb.append("<p>Total: ").append(formatTzs(order.getTotalAmount())).append("</p>");
        sb.append("<p>Shipping address: ").append(escapeNullable(order.getShippingAddress())).append("</p>");
        sb.append("<h3>Items</h3>");
        sb.append("<table style='width:100%; border-collapse: collapse;'>");
        sb.append("<thead><tr><th style='border:1px solid #ddd; padding:8px'>Product</th><th style='border:1px solid #ddd; padding:8px'>Qty</th><th style='border:1px solid #ddd; padding:8px'>Price (TZS)</th></tr></thead>");
        sb.append("<tbody>");
        for (OrderItemDto it : items) {
            BigDecimal total = it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
            sb.append("<tr>");
            sb.append("<td style='border:1px solid #ddd; padding:8px'>").append(it.getProductName()).append("</td>");
            sb.append("<td style='border:1px solid #ddd; padding:8px; text-align:center'>").append(it.getQuantity()).append("</td>");
            sb.append("<td style='border:1px solid #ddd; padding:8px; text-align:right'>").append(formatTzs(total)).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("<p>Karibu tena! — Thank you for shopping with us.</p>");
        return sb.toString();
    }

    private String formatTzs(BigDecimal amount) {
        if (amount == null) return "0.00 TZS";
        // Basic formatting — you can use NumberFormat for locale-specific formatting
        return String.format("%,.2f TZS", amount);
    }

    private String escapeNullable(String s) {
        return s == null ? "-" : s;
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML

        // From address should be configured in application.properties (spring.mail.username)
        mailSender.send(message);
    }
}