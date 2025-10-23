package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * Apply a coupon code to a user's cart total.
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyCoupon(@RequestBody ApplyCouponRequest request) {
        try {
            ApplyCouponResponse response = couponService.applyCoupon(
                    request.getUserId(),
                    request.getCouponCode(),
                    request.getCartTotal()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", ex.getMessage()));
        }
    }

    /**
     * Endpoint to create a new coupon.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CreateCouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply/complex")
    public ResponseEntity<CouponApplyResponse> applyCoupon(@RequestBody CouponApplyRequest request) {
        CouponApplyResponse response = couponService.applyCoupon(request);
        return ResponseEntity.ok(response);
    }

}
