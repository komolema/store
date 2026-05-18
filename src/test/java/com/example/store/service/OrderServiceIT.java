package com.example.store.service;

import com.example.store.entity.Order;
import com.example.store.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
 class OrderServiceIT {

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
	OrderService orderService;

	@Autowired
	OrderRepository orderRepository;

	@BeforeEach
	void beforeEach() {
		orderRepository.deleteAll();
	}

	private String randomAlpha(Random rnd, int max) {
		int len = rnd.nextInt(max - 5 + 1) + 5;
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
			String desc = randomAlpha(rnd, 20);

			orderRepository.deleteAll();
			Order o = new Order();
			o.setDescription(desc);
			orderRepository.save(o);

			List<Order> all = orderService.findAll();
			assertThat(all).extracting(Order::getDescription).contains(desc);

			var found = orderService.findById(o.getId());
			assertThat(found).isPresent();
		}
	}

}