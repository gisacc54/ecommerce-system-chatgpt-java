package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.ApplyCouponResponse;
import com.ecommerce.ecommerce.dto.CreateCouponRequest;
import com.ecommerce.ecommerce.dto.CreateCouponResponse;
import com.ecommerce.ecommerce.entity.Coupon;
import com.ecommerce.ecommerce.repository.CouponRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponService(CouponRepository couponRepository, UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    /**
     * Validate and apply coupon to cart total.
     */
    @Transactional
    public ApplyCouponResponse applyCoupon(Long userId, String code, BigDecimal cartTotal) {
        // 1. Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Verify coupon exists and is valid
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit exceeded");
        }

        // 3. Calculate discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (coupon.getDiscountPercent() != null) {
            discountAmount = cartTotal.multiply(coupon.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100));
        }
        if (coupon.getDiscountFixed() != null) {
            discountAmount = discountAmount.add(coupon.getDiscountFixed());
        }

        // Ensure discount does not exceed cart total
        if (discountAmount.compareTo(cartTotal) > 0) {
            discountAmount = cartTotal;
        }

        BigDecimal adjustedTotal = cartTotal.subtract(discountAmount);

        // 4. Update coupon usage count
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        // 5. Return response
        return new ApplyCouponResponse(cartTotal, discountAmount, adjustedTotal, coupon.getCode());
    }


    /**
     * Create a new coupon in the system.
     */
    @Transactional
    public CreateCouponResponse createCoupon(CreateCouponRequest request) {
        // 1. Check if code is unique
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new RuntimeException("Coupon code already exists");
        }

        // 2. Build new Coupon entity
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDiscountPercent(request.getDiscountPercent());
        coupon.setDiscountFixed(request.getDiscountFixed());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setUsedCount(0); // initial usage
        coupon.setExpiresAt(request.getExpiresAt());

        // 3. Save to database
        Coupon saved = couponRepository.save(coupon);

        // 4. Return response with full coupon details
        return new CreateCouponResponse(
                saved.getId(),
                "Coupon created successfully",
                saved.getCode(),
                saved.getDiscountPercent(),
                saved.getDiscountFixed(),
                saved.getUsageLimit(),
                saved.getExpiresAt()
        );
    }
}
