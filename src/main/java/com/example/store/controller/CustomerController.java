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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerMapper customerMapper;
    private final CustomerService customerService;
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @GetMapping
    public Page<CustomerDTO> getAllCustomers(@RequestParam(required = false) String name, @PageableDefault(size = 20) Pageable pageable) {
        log.debug("getAllCustomers called with name={}", name);
        if (name != null && !name.isBlank()) {
            var results = customerService.findByNameSubstring(name, pageable);
            log.info("search found page {} of customers for query={}", results.getNumber(), name);
            return results.map(customerMapper::customerToCustomerDTO);
        }
        var page = customerService.findAll(pageable);
        log.info("returning page {} of customers (full list)", page.getNumber());
        return page.map(customerMapper::customerToCustomerDTO);
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
