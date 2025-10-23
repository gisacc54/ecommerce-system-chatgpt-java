package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.AddToCartRequest;
import com.ecommerce.ecommerce.dto.CartTotalResponse;
import com.ecommerce.ecommerce.entity.Cart;
import com.ecommerce.ecommerce.entity.Coupon;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.CartRepository;
import com.ecommerce.ecommerce.repository.CouponRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository, CouponRepository couponRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantityToAdd) {
        // 1. Fetch the user entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch the product entity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantityToAdd <= 0) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        // 3. Check if the product is already in the cart
        Optional<Cart> optionalCart = cartRepository.findByUserAndProduct(user, product);

        Cart cartItem;
        if (optionalCart.isPresent()) {
            // Update existing cart item
            cartItem = optionalCart.get();
            int newQuantity = cartItem.getQuantity() + quantityToAdd;
            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Not enough stock available");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            cartItem = new Cart();
            cartItem.setUser(user);       // set User entity
            cartItem.setProduct(product); // set Product entity
            cartItem.setQuantity(quantityToAdd);
        }

        // 4. Save cart item
        return cartRepository.save(cartItem);
    }

    /**
     * Calculate cart total for a user, applying an optional coupon code.
     */
    @Transactional(readOnly = true)
    public CartTotalResponse calculateCartTotal(Long userId, String couponCode) {
        // 1. Fetch all cart items for the user
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            return new CartTotalResponse(BigDecimal.ZERO, 0, BigDecimal.ZERO);
        }

        // 2. Sum total: quantity * price for each product
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;

        for (Cart cartItem : cartItems) {
            var productOpt = productRepository.findById(cartItem.getProduct().getId());
            if (productOpt.isEmpty()) continue; // skip missing products
            var product = productOpt.get();

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);
            itemCount += cartItem.getQuantity();
        }

        BigDecimal discountApplied = BigDecimal.ZERO;

        // 3. Apply coupon discount if provided
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode)
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

            // Check expiration
            if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Coupon code has expired");
            }

            // Check usage limit
            if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
                throw new RuntimeException("Coupon usage limit exceeded");
            }

            // Apply discount
            if (coupon.getDiscountPercent() != null) {
                discountApplied = total.multiply(coupon.getDiscountPercent().divide(BigDecimal.valueOf(100)));
            } else if (coupon.getDiscountFixed() != null) {
                discountApplied = coupon.getDiscountFixed();
            }

            total = total.subtract(discountApplied);

            // Ensure total doesn't go below zero
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }
        }

        return new CartTotalResponse(total, itemCount, discountApplied);
    }

    /**
     * Empties all cart items for a specific user.
     *
     * @param userId the ID of the authenticated user
     * @return the number of items deleted
     */
    @Transactional
    public int emptyCart(Long userId) {
        // Fetch all cart items for the user
        List<Cart> userCartItems = cartRepository.findByUserId(userId);

        if (userCartItems.isEmpty()) {
            return 0; // nothing to delete
        }

        // Delete all user's cart items
        cartRepository.deleteAll(userCartItems);

        return userCartItems.size();
    }
}