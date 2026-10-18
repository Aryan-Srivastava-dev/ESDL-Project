package com.project.billingsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Invoice invoice;

    private double amountPaid;
    private LocalDateTime paymentTimeStamp;
    private String status;
    private String mode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDateTime getPaymentTimeStamp() {
        return paymentTimeStamp;
    }

    public void setPaymentTimeStamp(LocalDateTime paymentTimeStamp) {
        this.paymentTimeStamp = paymentTimeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
