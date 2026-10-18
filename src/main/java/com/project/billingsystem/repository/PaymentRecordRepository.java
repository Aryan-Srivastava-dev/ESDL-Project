package com.project.billingsystem.repository;

import com.project.billingsystem.entity.Invoice;
import com.project.billingsystem.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    List<Invoice> findByInvoice_Id(long invoiceId);
}
