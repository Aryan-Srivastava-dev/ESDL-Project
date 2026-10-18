package com.project.billingsystem.controller;

import com.project.billingsystem.entity.Invoice;
import com.project.billingsystem.entity.Subscription;
import com.project.billingsystem.service.InvoiceService;
import com.project.billingsystem.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final InvoiceService invoiceService;

    public SubscriptionController(SubscriptionService subscriptionService, InvoiceService invoiceService) {
        this.subscriptionService = subscriptionService;
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@RequestParam long customerId, @RequestParam long planId) {
        Subscription newSubscription = subscriptionService.createSubscription(customerId, planId);

        return new ResponseEntity<>(newSubscription, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Subscription> cancelSubscription(@PathVariable("id") long subscriptionId) {
        Subscription cancelledSubscription = subscriptionService.cancelSubscription(subscriptionId);

        return ResponseEntity.ok(cancelledSubscription);
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<Subscription> renewSubscription(@PathVariable("id") long subscriptionId) {
        Subscription renewedSubscription = subscriptionService.renewSubscription(subscriptionId);

        return ResponseEntity.ok(renewedSubscription);
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<Invoice>> getSubscriptionInvoices(@PathVariable("id") long subscriptionId) {
        List<Invoice> invoices = invoiceService.getInvoiceBySubscription(subscriptionId);

        return ResponseEntity.ok(invoices);
    }
}
