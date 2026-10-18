package com.project.billingsystem.service;

import com.project.billingsystem.entity.Invoice;
import com.project.billingsystem.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> getInvoiceBySubscription(long subscriptionId) {
        return invoiceRepository.findBySubscription_Id(subscriptionId);
    }
}
