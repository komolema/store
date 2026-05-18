package com.example.store.service;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductServiceIT {

	@DynamicPropertySource
	static void registerH2Properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> "jdbc:h2:mem:store;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
		registry.add("spring.datasource.username", () -> "sa");
		registry.add("spring.datasource.password", () -> "");
		registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
		registry.add("spring.sql.init.mode", () -> "never");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		registry.add("spring.liquibase.enabled", () -> "false");
	}

	@Autowired
	ProductService productService;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ProductMapper productMapper;

	@BeforeEach
	void beforeEach() {
		orderRepository.deleteAll();
		productRepository.deleteAll();
	}

	private String randomAlpha(Random rnd, int min, int max) {
		int len = rnd.nextInt(max - min + 1) + min;
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = (char) ('a' + rnd.nextInt(26));
			sb.append(c);
		}
		return sb.toString();
	}

	@Test
	void propertyStyleRandomizedSaveAndFindTest() {
		Random rnd = new Random(12345);
		for (int i = 0; i < 200; i++) {
			String desc = randomAlpha(rnd, 5, 20);

			productRepository.deleteAll();
			Product p = new Product();
			p.setDescription(desc);
			productRepository.save(p);

			List<Product> all = productService.findAll();
			assertThat(all).extracting(Product::getDescription).contains(desc);

			var found = productService.findById(p.getId());
			assertThat(found).isPresent();
		}
	}

	@Test
	@Transactional
	void findAllProductsAsDTOWithOrderIds() {
		Order order = new Order();
		order.setDescription("Order with product");
		order = orderRepository.save(order);

		Product product = new Product();
		product.setDescription("Attached Product");
		product.getOrders().add(order);

        Page<Product> page = productService.findAll(PageRequest.of(0, 10));
		List<ProductDTO> dtos = productMapper.productsToProductDTOs(page.getContent());

		assertThat(dtos).hasSize(1);
		ProductDTO dto = dtos.get(0);
		assertThat(dto.getDescription()).isEqualTo("Attached Product");
		assertThat(dto.getOrderIds()).contains(order.getId());
	}

}