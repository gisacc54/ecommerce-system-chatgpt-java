package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.SalesReportDto;
import com.ecommerce.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class SalesReportService {

    private final OrderRepository orderRepository;

    public SalesReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Generate sales report for a given date or period.
     *
     * @param date input date in yyyy-MM-dd format
     * @return SalesReportDto containing total orders and total sales
     */
    public SalesReportDto generateReport(LocalDate date) {
        // Define the start and end of the day
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        return orderRepository.getSalesReport(start, end);
    }
}