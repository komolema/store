package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.CustomerService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerService customerService;
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @GetMapping
    public List<CustomerDTO> getAllCustomers(@RequestParam(required = false) String name) {
        log.debug("getAllCustomers called with name={}", name);
        if (name != null && !name.isBlank()) {
            var results = customerService.findByNameSubstring(name);
            log.info("search found {} customers for query={}", results.size(), name);
            return customerMapper.customersToCustomerDTOs(results);
        }
        var all = customerService.findAll();
        log.info("returning {} customers (full list)", all.size());
        return customerMapper.customersToCustomerDTOs(all);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(@RequestBody Customer customer) {
        log.info("creating customer name={}", customer.getName());
        var saved = customerService.save(customer);
        log.debug("created customer id={}", saved.getId());
        return customerMapper.customerToCustomerDTO(saved);
    }

    // removed unused placeholder endpoint
}
