package com.project.billingsystem.service;

import com.project.billingsystem.entity.Invoice;
import com.project.billingsystem.entity.PaymentRecord;
import com.project.billingsystem.exception.BusinessRuleException;
import com.project.billingsystem.exception.ResourceNotFoundException;
import com.project.billingsystem.repository.InvoiceRepository;
import com.project.billingsystem.repository.PaymentRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRecordRepository paymentRecordRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRecordRepository paymentRecordRepository, InvoiceRepository invoiceRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public PaymentRecord processPayment(long invoiceId, PaymentRecord paymentRecord) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if(invoice.getSubscription().getStatus().equals("CANCELLED")) {
            throw new BusinessRuleException("Cannot pay for a cancelled subscription");
        }

        if ("PAID".equals(invoice.getStatus())) {
            throw new BusinessRuleException("Invoice is already paid");
        }

        if (Double.compare(paymentRecord.getAmountPaid(), invoice.getAmountDue()) != 0) {
            throw new BusinessRuleException("Payment amount must match the invoice amount due");
        }

        paymentRecord.setInvoice(invoice);
        paymentRecord.setPaymentTimeStamp(LocalDateTime.now());

        if(paymentRecord.getStatus().equals("SUCCESS")) {
            invoice.setStatus("PAID");
            invoiceRepository.save(invoice);
        }

        return paymentRecordRepository.save(paymentRecord);
    }

    public List<PaymentRecord> getPaymentsByInvoice(long invoiceId) {
        return paymentRecordRepository.findByInvoice_Id(invoiceId);
    }
}
