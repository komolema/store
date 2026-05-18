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
		product = productRepository.save(product);

        Page<Product> page = productService.findAllWithOrders(PageRequest.of(0, 10));
		List<ProductDTO> dtos = productMapper.productsToProductDTOs(page.getContent());

		assertThat(dtos).hasSize(1);
		ProductDTO dto = dtos.get(0);
		assertThat(dto.getDescription()).isEqualTo("Attached Product");
		assertThat(dto.getOrderIds()).contains(order.getId());
	}

	@Test
	@Transactional
	void findProductWithMultipleOrders() {
		// Create multiple orders
		Order order1 = new Order();
		order1.setDescription("Order 1");
		order1 = orderRepository.save(order1);

		Order order2 = new Order();
		order2.setDescription("Order 2");
		order2 = orderRepository.save(order2);

		Order order3 = new Order();
		order3.setDescription("Order 3");
		order3 = orderRepository.save(order3);

		// Create product with multiple orders
		Product product = new Product();
		product.setDescription("Multi-Order Product");
		product.getOrders().add(order1);
		product.getOrders().add(order2);
		product.getOrders().add(order3);
		product = productRepository.save(product);

		// Query and verify
		var found = productService.findByIdWithOrders(product.getId());
		assertThat(found).isPresent();

		ProductDTO dto = productMapper.productToProductDTO(found.get());
		assertThat(dto.getDescription()).isEqualTo("Multi-Order Product");
		assertThat(dto.getOrderIds()).hasSize(3)
				.contains(order1.getId(), order2.getId(), order3.getId());
	}

	@Test
	@Transactional
	void findProductWithoutOrders() {
		Product product = new Product();
		product.setDescription("Solo Product");
		product = productRepository.save(product);

		var found = productService.findByIdWithOrders(product.getId());
		assertThat(found).isPresent();

		ProductDTO dto = productMapper.productToProductDTO(found.get());
		assertThat(dto.getDescription()).isEqualTo("Solo Product");
		assertThat(dto.getOrderIds()).isEmpty();
	}

	@Test
	@Transactional
	void findAllWithOrdersMixedScenarios() {
		// Create orders
		Order order1 = new Order();
		order1.setDescription("Order 1");
		order1 = orderRepository.save(order1);

		Order order2 = new Order();
		order2.setDescription("Order 2");
		order2 = orderRepository.save(order2);

		// Product A: 2 orders
		Product productA = new Product();
		productA.setDescription("Product A");
		productA.getOrders().add(order1);
		productA.getOrders().add(order2);
		productA = productRepository.save(productA);

		// Product B: 1 order
		Product productB = new Product();
		productB.setDescription("Product B");
		productB.getOrders().add(order1);
		productB = productRepository.save(productB);

		// Product C: 0 orders
		Product productC = new Product();
		productC.setDescription("Product C");
		productC = productRepository.save(productC);

		// Query all with orders
		Page<Product> page = productService.findAllWithOrders(PageRequest.of(0, 20));
		List<ProductDTO> dtos = productMapper.productsToProductDTOs(page.getContent());

		assertThat(dtos).hasSize(3);

		ProductDTO dtoA = dtos.stream()
				.filter(d -> d.getDescription().equals("Product A"))
				.findFirst()
				.orElseThrow();
		assertThat(dtoA.getOrderIds()).hasSize(2).contains(order1.getId(), order2.getId());

		ProductDTO dtoB = dtos.stream()
				.filter(d -> d.getDescription().equals("Product B"))
				.findFirst()
				.orElseThrow();
		assertThat(dtoB.getOrderIds()).hasSize(1).contains(order1.getId());

		ProductDTO dtoC = dtos.stream()
				.filter(d -> d.getDescription().equals("Product C"))
				.findFirst()
				.orElseThrow();
		assertThat(dtoC.getOrderIds()).isEmpty();
	}

	@Test
	@Transactional
	void manyToManyRelationshipPersistence() {
		// Create bidirectional many-to-many relationship
		Order order = new Order();
		order.setDescription("Order");
		order = orderRepository.save(order);

		Product product = new Product();
		product.setDescription("Product");
		product.getOrders().add(order);
		product = productRepository.save(product);

		// Clear session and re-fetch to ensure persistence
		productRepository.flush();

		// Retrieve and verify
		var retrieved = productService.findByIdWithOrders(product.getId());
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getOrders()).hasSize(1)
				.extracting(Order::getDescription)
				.contains("Order");
	}

}

