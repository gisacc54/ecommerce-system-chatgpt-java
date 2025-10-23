package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.SalesReportDto;
import com.ecommerce.ecommerce.service.SalesReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
public class SalesReportController {

    private final SalesReportService salesReportService;

    public SalesReportController(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    /**
     * GET /reports/sales?period={date}
     *
     * @param period date in yyyy-MM-dd format
     * @return JSON report with totalOrders and totalSalesAmount in TZS
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesReportDto> getSalesReport(
            @RequestParam("period")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period
    ) {
        // Generate the sales report
        SalesReportDto report = salesReportService.generateReport(period);

        return ResponseEntity.ok(report);
    }
}