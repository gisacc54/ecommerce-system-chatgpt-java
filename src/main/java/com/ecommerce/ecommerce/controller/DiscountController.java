package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.ApplyDiscountRequest;
import com.ecommerce.ecommerce.dto.ApplyDiscountResponse;
import com.ecommerce.ecommerce.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discounts")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    // POST /discounts/apply
    @PostMapping("/apply")
    public ResponseEntity<?> applyDiscount(@Valid @RequestBody ApplyDiscountRequest request) {
        // Delegate calculation to service
        ApplyDiscountResponse response = discountService.applyDiscount(request);
        return ResponseEntity.ok(response);
    }
}