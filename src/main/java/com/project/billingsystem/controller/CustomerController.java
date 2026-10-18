package com.project.billingsystem.controller;

import com.project.billingsystem.entity.Customer;
import com.project.billingsystem.entity.Subscription;
import com.project.billingsystem.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer newCustomer = customerService.createCustomer(customer);
        return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/subscriptions")
    public ResponseEntity<List<Subscription>> getCustomerSubscriptions(@PathVariable("id") long customerId) {
        List<Subscription> subscriptions = customerService.getCustomerSubscriptions(customerId);

        return ResponseEntity.ok(subscriptions);
    }
}
