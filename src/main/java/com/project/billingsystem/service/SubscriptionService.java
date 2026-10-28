package com.project.billingsystem.service;

import com.project.billingsystem.entity.Customer;
import com.project.billingsystem.entity.Invoice;
import com.project.billingsystem.entity.Plan;
import com.project.billingsystem.entity.Subscription;
import com.project.billingsystem.exception.BusinessRuleException;
import com.project.billingsystem.exception.ResourceNotFoundException;
import com.project.billingsystem.repository.CustomerRepository;
import com.project.billingsystem.repository.InvoiceRepository;
import com.project.billingsystem.repository.PlanRepository;
import com.project.billingsystem.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, CustomerRepository customerRepository, PlanRepository planRepository, InvoiceRepository invoiceRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
        this.planRepository = planRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public Subscription createSubscription(long customerId, long planId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + planId));

        if (subscriptionRepository.existsByCustomer_IdAndPlan_IdAndStatus(customerId, planId, "ACTIVE")) {
            throw new BusinessRuleException("Customer already has an active subscription to this plan");
        }

        Subscription subscription = new Subscription();
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        if(plan.getBillingInterval().equals("MONTHLY"))
            subscription.setNextRenewalDate(LocalDate.now().plusMonths(1));
        else if(plan.getBillingInterval().equals("YEARLY"))
            subscription.setNextRenewalDate(LocalDate.now().plusYears(1));
        if(plan.getBillingInterval().equals("MONTHLY"))
            subscription.setExpiryDate(LocalDate.now().plusMonths(1));
        else if(plan.getBillingInterval().equals("YEARLY"))
            subscription.setExpiryDate(LocalDate.now().plusYears(1));
        subscription.setStatus("ACTIVE");

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        Invoice invoice = new Invoice();
        invoice.setSubscription(savedSubscription);
        invoice.setAmountDue(plan.getPrice());
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(7));
        invoice.setStatus("PENDING");

        invoiceRepository.save(invoice);

        return savedSubscription;
    }

    public Subscription cancelSubscription(long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        if ("CANCELLED".equals(subscription.getStatus())) {
            throw new BusinessRuleException("Subscription is already cancelled");
        }

        subscription.setStatus("CANCELLED");
        return subscriptionRepository.save(subscription);
    }

    public Subscription renewSubscription(long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));

        if ("CANCELLED".equals(subscription.getStatus())) {
            throw new BusinessRuleException("Cannot renew a cancelled subscription");
        }

        if(LocalDate.now().isBefore(subscription.getNextRenewalDate())) {
            throw new BusinessRuleException("Subscription cannot be renewed before its next renewal date: " + subscription.getNextRenewalDate());
        }

        Plan plan = subscription.getPlan();
        LocalDate currentRenewal = subscription.getNextRenewalDate() != null ? subscription.getNextRenewalDate() : LocalDate.now();
        if(plan.getBillingInterval().equals("MONTHLY"))
            subscription.setNextRenewalDate(currentRenewal.plusMonths(1));
        else if(plan.getBillingInterval().equals("YEARLY"))
            subscription.setNextRenewalDate(currentRenewal.plusYears(1));
        if(plan.getBillingInterval().equals("MONTHLY"))
            subscription.setExpiryDate(currentRenewal.plusMonths(1));
        else if(plan.getBillingInterval().equals("YEARLY"))
            subscription.setExpiryDate(currentRenewal.plusYears(1));
        subscription.setStatus("ACTIVE");

        Invoice invoice = new Invoice();
        invoice.setSubscription(subscription);
        invoice.setAmountDue(plan.getPrice());
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(7));
        invoice.setStatus("PENDING");

        invoiceRepository.save(invoice);

        return subscriptionRepository.save(subscription);
    }
}
