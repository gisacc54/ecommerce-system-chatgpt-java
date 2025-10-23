package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.CancelOrderResponse;
import com.ecommerce.ecommerce.dto.PlaceOrderRequest;
import com.ecommerce.ecommerce.dto.PlaceOrderResponse;
import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(CartRepository cartRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository, PaymentRepository paymentRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
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
        response.setRefundProcessed(refundProcessed);

        return response;
    }
}