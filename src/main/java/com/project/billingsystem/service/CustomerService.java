package com.project.billingsystem.service;

import com.project.billingsystem.entity.Customer;
import com.project.billingsystem.entity.Subscription;
import com.project.billingsystem.repository.CustomerRepository;
import com.project.billingsystem.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;

    public CustomerService(CustomerRepository customerRepository, SubscriptionRepository subscriptionRepository) {
        this.customerRepository = customerRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Subscription> getCustomerSubscriptions(long customerId) {
        if(!customerRepository.existsById(customerId))
            throw new RuntimeException("Customer not found with id: " + customerId);

        return subscriptionRepository.findByCustomer_Id(customerId);
    }
}
