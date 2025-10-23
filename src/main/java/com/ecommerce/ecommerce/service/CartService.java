package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CartItemRepository cartItemRepository;



    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository, CouponRepository couponRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.cartItemRepository = cartItemRepository;
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

    @Transactional
    public CartAddResponseDto addToCartWithRecs(CartAddRequestDto request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Locking and updating or creating cart item
        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUser(user);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);

        // Fetch updated cart items
        List<CartItemDto> cartItems = cartItemRepository.findByUser(user)
                .stream()
                .map(item -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setProductId(item.getProduct().getId());
                    dto.setProductName(item.getProduct().getName());
                    dto.setQuantity(item.getQuantity());
                    dto.setPriceTZS(item.getProduct().getPriceTZS());
                    return dto;
                })
                .collect(Collectors.toList());

        // Calculate total TZS
        BigDecimal totalTZS = cartItems.stream()
                .map(ci -> ci.getPriceTZS().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate simple recommendations (based on other products)
        List<CartItemDto> recommendedProducts = productRepository.findAll()
                .stream()
                .filter(p -> !cartItems.stream().anyMatch(ci -> ci.getProductId().equals(p.getId())))
                .limit(5)
                .map(p -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setProductId(p.getId());
                    dto.setProductName(p.getName());
                    dto.setPriceTZS(p.getPriceTZS());
                    dto.setQuantity(1);
                    return dto;
                })
                .collect(Collectors.toList());

        CartAddResponseDto response = new CartAddResponseDto();
        response.setCartItems(cartItems);
        response.setTotalTZS(totalTZS);
        response.setRecommendedProducts(recommendedProducts);

        return response;
    }

    @Transactional(readOnly = true)
    public CartTotalResponse getComplexCartTotal(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        CartTotalResponse response = new CartTotalResponse();

        List<CartTotalResponse.CartItemDetail> itemDetails = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Fetch product prices and calculate subtotal
        for (Cart item : cartItems) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct().getId()));

            BigDecimal itemTotal = product.getPriceTZS().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            CartTotalResponse.CartItemDetail detail = new CartTotalResponse.CartItemDetail();
            detail.setProductId(product.getId());
            detail.setProductName(product.getName());
            detail.setPriceTZS(product.getPriceTZS());
            detail.setQuantity(item.getQuantity());
            detail.setTotalTZS(itemTotal);
            itemDetails.add(detail);
        }

        // Apply discounts (e.g., from coupons or external promo API)
        BigDecimal discount = calculateDiscount(userId, cartItems);

        // Calculate taxes (region-based)
        BigDecimal taxRate = user.getRegion().equalsIgnoreCase("Zanzibar") ? new BigDecimal("0.18") : new BigDecimal("0.15");
        BigDecimal tax = subtotal.subtract(discount).multiply(taxRate).setScale(0, RoundingMode.HALF_UP);

        // Fetch shipping cost
        BigDecimal shipping = BigDecimal.valueOf(2000);

        // Total
        BigDecimal total = subtotal.subtract(discount).add(tax).add(shipping);

        // Set response
        response.setItems(itemDetails);
        response.setSubtotalTZS(subtotal);
        response.setDiscountTZS(discount);
        response.setTaxTZS(tax);
        response.setShippingTZS(shipping);
        response.setTotalTZS(total);

        return response;
    }

    private BigDecimal calculateDiscount(Long userId, List<Cart> cartItems) {
        // Example: 5% off for promotional purposes
        BigDecimal subtotal = cartItems.stream()
                .map(ci -> ci.getProduct().getPriceTZS().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return subtotal.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
    }
}