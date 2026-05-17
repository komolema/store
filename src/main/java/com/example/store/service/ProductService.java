package com.example.store.service;

import com.example.store.entity.Product;
import com.example.store.repository.ProductRepository;

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
public class ProductService {

	private final ProductRepository productRepository;
	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	public List<Product> findAll() {
		return productRepository.findAll();
	}

	public Page<Product> findAll(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	public Product save(Product product) {
		log.debug("saving product description={}", product.getDescription());
		var saved = productRepository.save(product);
		log.debug("saved product id={}", saved.getId());
		return saved;
	}

	public Optional<Product> findById(Long id) {
		return productRepository.findById(id);
	}

}