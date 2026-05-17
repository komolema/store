package com.example.store.service;

import com.example.store.entity.Order;
import com.example.store.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public Page<Order> findAll(Pageable pageable) {
		return orderRepository.findAll(pageable);
	}

	public Order save(Order order) {
		log.debug("saving order description={}", order.getDescription());
		var saved = orderRepository.save(order);
		log.debug("saved order id={}", saved.getId());
		return saved;
	}

	public Optional<Order> findById(Long id) {
		return orderRepository.findById(id);
	}

}