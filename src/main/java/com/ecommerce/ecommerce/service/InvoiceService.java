package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.InvoiceDto;
import com.ecommerce.ecommerce.dto.InvoiceItemDto;
import com.ecommerce.ecommerce.entity.Order;
import com.ecommerce.ecommerce.entity.OrderItem;
import com.ecommerce.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InvoiceService {

    private final OrderRepository orderRepository;

    public InvoiceService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Generate a simple JSON invoice
     */
    public InvoiceDto generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Map order items to InvoiceItemDto
        List<InvoiceItemDto> items = order.getOrderItems()
                .stream()
                .map(this::mapToInvoiceItem)
                .collect(Collectors.toList());

        return new InvoiceDto(
                order.getId(),
                order.getUser().getName(),
                order.getShippingAddress(),
                order.getTotalAmount(),
                items
        );
    }



    private InvoiceItemDto mapToInvoiceItem(OrderItem item) {
        BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new InvoiceItemDto(
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice(),
                totalPrice
        );
    }

    /**
     * Generate a detailed invoice including PDF/QR, taxes, and JSON
     */
    public InvoiceDto generateDetailedInvoice(Long orderId) {
        // 1. Fetch order, throw exception if not found
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. Map order items to InvoiceItemDto with taxes
        BigDecimal taxRate = new BigDecimal("0.18"); // 18% VAT for example
        List<InvoiceItemDto> items = order.getOrderItems().stream()
                .map(item -> {
                    BigDecimal tax = item.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            .multiply(taxRate);
                    return new InvoiceItemDto(item.getProduct().getName(),
                            item.getQuantity(),
                            item.getPrice(),
                            tax);
                })
                .collect(Collectors.toList());

        // 3. Calculate total tax and total amount


        BigDecimal totalAmount = items.stream()
                .map(InvoiceItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Generate QR code for payment
        String qrCodeBase64 = generatePaymentQRCode(order);

        // 5. Return detailed invoice DTO
        return new InvoiceDto(
                order.getId(),
                order.getUser().getName(),
                order.getShippingAddress(),
                totalAmount,
                items,
                qrCodeBase64
        );
    }

    /**
     * Generate QR code with Base64 encoding for embedding in PDF/JSON
     */
    private String generatePaymentQRCode(Order order) {
        try {
            String qrText = "PAYMENT: ORDER " + order.getId() +
                    " AMOUNT " + order.getTotalAmount() + " TZS";

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(qrText, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            // Log error and return empty QR
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate PDF invoice with QR code and detailed info
     */
    public byte[] generateInvoicePdf(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        BigDecimal taxRate = new BigDecimal("0.18"); // 18% VAT
        List<InvoiceItemDto> items = order.getOrderItems().stream()
                .map(item -> {
                    BigDecimal tax = item.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            .multiply(taxRate);
                    return new InvoiceItemDto(item.getProduct().getName(),
                            item.getQuantity(),
                            item.getPrice(),
                            tax);
                }).collect(Collectors.toList());


        BigDecimal totalAmount = items.stream()
                .map(InvoiceItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add title
            Font titleFont = new Font(Font.SERIF, 18, Font.BOLD);
            Paragraph title = new Paragraph("Invoice");
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // blank line

            // Add order info
            document.add(new Paragraph("Order ID: " + order.getId()));
            document.add(new Paragraph("Customer: " + order.getUser().getName()));
            document.add(new Paragraph("Shipping Address: " + order.getShippingAddress()));
            document.add(new Paragraph(" "));

            // Add table of items
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{3, 1, 2, 2, 2});
            addTableHeader(table);

            for (InvoiceItemDto item : items) {
                table.addCell(item.getProductName());
                table.addCell(item.getQuantity().toString());
                table.addCell(item.getUnitPrice().toString() + " TZS");
                table.addCell(item.getTotalPrice().toString() + " TZS");
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Add totals
            document.add(new Paragraph("Total Amount: " + totalAmount + " TZS"));
            document.add(new Paragraph(" "));

            // Add QR code
            Image qrImage = Image.getInstance(generateQrCode(order, 150, 150));
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Product", "Qty", "Unit Price", "Tax", "Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(Color.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private byte[] generateQrCode(Order order, int width, int height) throws Exception {
        String qrText = "PAYMENT: ORDER " + order.getId() +
                " AMOUNT " + order.getTotalAmount() + " TZS";

        BitMatrix matrix = new MultiFormatWriter().encode(qrText, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }
}