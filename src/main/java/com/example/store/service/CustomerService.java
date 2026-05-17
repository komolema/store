package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepository;
	private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

	public List<Customer> findByNameSubstring(String query) {
		log.debug("findByNameSubstring called with query={}", query);
		if (query == null) return List.of();
		var results = customerRepository.findByNameContainingIgnoreCase(query);
		log.debug("findByNameSubstring returning {} results", results.size());
		return results;
	}

	public List<Customer> findAll() {
		return customerRepository.findAll();
	}

	public Page<Customer> findAll(Pageable pageable) {
		return customerRepository.findAll(pageable);
	}

	public Page<Customer> findByNameSubstring(String query, Pageable pageable) {
		if (query == null || query.isBlank()) {
			return customerRepository.findAll(pageable);
		}
		return customerRepository.findByNameContainingIgnoreCase(query, pageable);
	}

	public Customer save(Customer customer) {
		log.debug("saving customer name={}", customer.getName());
		var saved = customerRepository.save(customer);
		log.debug("saved customer id={}", saved.getId());
		return saved;
	}

}

