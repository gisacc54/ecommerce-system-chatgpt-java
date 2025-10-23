package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.InvoiceDto;
import com.ecommerce.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.ecommerce.service.InvoiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * GET /orders/{id}/invoice - JSON invoice
     */
    @GetMapping("/{id}/invoice")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.generateInvoice(id);
        return ResponseEntity.ok(invoice);
    }


    /**
     * GET /orders/{id}/invoice/detailed
     */
    @GetMapping("/{id}/invoice/detailed")
    public ResponseEntity<InvoiceDto> getDetailedInvoice(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.generateDetailedInvoice(id);
        return ResponseEntity.ok(invoice);
    }

    /**
     * GET /orders/{id}/invoice/detailed/pdf
     */
    @GetMapping("/{id}/invoice/detailed/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


    /**
     * Exception handler for order not found
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}