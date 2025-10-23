package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.ApplyDiscountRequest;
import com.ecommerce.ecommerce.dto.ApplyDiscountResponse;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class DiscountService {

    // Calculates discounted price based on the given percent
    public ApplyDiscountResponse applyDiscount(ApplyDiscountRequest request) {
        BigDecimal totalAmount = request.getTotalAmount();
        BigDecimal discountPercent = request.getDiscountPercent();

        // Calculate discount = totalAmount * discountPercent / 100
        BigDecimal discountAmount = totalAmount.multiply(discountPercent).divide(BigDecimal.valueOf(100));

        // Compute final discounted amount
        BigDecimal discountedAmount = totalAmount.subtract(discountAmount);

        return new ApplyDiscountResponse(
                totalAmount,
                discountPercent,
                discountedAmount,
                String.format("Discount of %s%% applied successfully", discountPercent)
        );
    }
}