package com.project.billingsystem.controller;

import com.project.billingsystem.entity.PaymentRecord;
import com.project.billingsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/invoices/{invoiceId}")
    public ResponseEntity<PaymentRecord> processPayment(@PathVariable long invoiceId, @RequestBody PaymentRecord paymentRecord) {
        PaymentRecord payment = paymentService.processPayment(invoiceId, paymentRecord);

        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<List<PaymentRecord>> getPaymentsForInvoice(@PathVariable long invoiceId) {
        List<PaymentRecord> payments = paymentService.getPaymentsByInvoice(invoiceId);

        return ResponseEntity.ok(payments);
    }
}
