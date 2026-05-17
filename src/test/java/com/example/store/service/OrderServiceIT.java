package com.example.store.service;

import com.example.store.entity.Order;
import com.example.store.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.DockerClientFactory;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderServiceIT {

	private static PostgreSQLContainer<?> postgres;

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		boolean useTc = false;
		String env = System.getenv("USE_TESTCONTAINERS");
		if (env != null) {
			useTc = Boolean.parseBoolean(env);
		} else {
			useTc = Boolean.parseBoolean(System.getProperty("useTestcontainers", "false"));
		}

		String external = System.getenv("USE_TESTCONTAINERS");
		if (external != null && external.equalsIgnoreCase("external")) {
			String url = System.getenv("SPRING_DATASOURCE_URL");
			String user = System.getenv("SPRING_DATASOURCE_USERNAME");
			String pass = System.getenv("SPRING_DATASOURCE_PASSWORD");
			if (url == null) {
				throw new IllegalStateException("USE_TESTCONTAINERS=external requires SPRING_DATASOURCE_URL to be set");
			}
			registry.add("spring.datasource.url", () -> url);
			registry.add("spring.datasource.username", () -> user == null ? "" : user);
			registry.add("spring.datasource.password", () -> pass == null ? "" : pass);
			registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
			registry.add("spring.sql.init.mode", () -> "never");
			return;
		}

		if (useTc) {
			if (!DockerClientFactory.instance().isDockerAvailable()) {
				throw new org.opentest4j.TestAbortedException("Docker not available; start Docker to run Testcontainers-based integration tests");
			}

			postgres = new PostgreSQLContainer<>("postgres:16.2")
					.withDatabaseName("store")
					.withUsername("admin")
					.withPassword("admin");
			postgres.start();

			registry.add("spring.datasource.url", postgres::getJdbcUrl);
			registry.add("spring.datasource.username", postgres::getUsername);
			registry.add("spring.datasource.password", postgres::getPassword);
			registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
			registry.add("spring.sql.init.mode", () -> "never");
		} else {
			registry.add("spring.datasource.url", () -> "jdbc:h2:mem:store;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
			registry.add("spring.datasource.username", () -> "sa");
			registry.add("spring.datasource.password", () -> "");
			registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
			registry.add("spring.sql.init.mode", () -> "never");
			registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		}
	}

	@Autowired
	OrderService orderService;

	@Autowired
	OrderRepository orderRepository;

	@BeforeEach
	void beforeEach() {
		orderRepository.deleteAll();
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