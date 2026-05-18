package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

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
class CustomerServiceIT {

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
	CustomerService customerService;

	@Autowired
	CustomerRepository customerRepository;

	@BeforeEach
	void beforeEach() {
		customerRepository.deleteAll();
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
	void propertyStyleRandomizedSearchTest() {
		Random rnd = new Random(12345);
		for (int i = 0; i < 200; i++) {
			String word = randomAlpha(rnd, 3, 12);
			String other = randomAlpha(rnd, 3, 12);
			String fragment = word.substring(0, Math.min(3, word.length()));
			String fullName = word + " " + other;

			customerRepository.deleteAll();
			Customer c = new Customer();
			c.setName(fullName);
			customerRepository.save(c);

			List<Customer> results = customerService.findByNameSubstring(fragment);
			assertThat(results).extracting(Customer::getId).contains(c.getId());
		}
	}

}
