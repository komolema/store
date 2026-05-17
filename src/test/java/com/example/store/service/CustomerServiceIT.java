package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

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
public class CustomerServiceIT {

	// We'll manage the PostgreSQLContainer lifecycle ourselves in DynamicPropertySource so
	// the container is started before the Spring context is initialized. This allows us to
	// abort/skip the tests early when Docker isn't available instead of failing when the
	// Testcontainers JUnit extension attempts to start containers.
	private static PostgreSQLContainer<?> postgres;

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		// Behavior controlled by environment variable USE_TESTCONTAINERS or system property
		// - If USE_TESTCONTAINERS=true (or -DuseTestcontainers=true) the test will attempt
		//   to start a PostgreSQL Testcontainer and will abort early if Docker isn't available.
		// - Otherwise (default) it uses an in-memory H2 database in Postgres compatibility mode
		//   so that tests are runnable by default on developer machines without Docker.
		boolean useTc = false;
		String env = System.getenv("USE_TESTCONTAINERS");
		if (env != null) {
			useTc = Boolean.parseBoolean(env);
		} else {
			useTc = Boolean.parseBoolean(System.getProperty("useTestcontainers", "false"));
		}

		// Special mode: if USE_TESTCONTAINERS is set to the literal value "external"
		// the tests will use externally-provided JDBC connection settings from
		// SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD. This allows running the
		// integration tests against a Postgres instance started with docker-compose
		// (or any other external DB) instead of using Testcontainers.
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
		} else {
			registry.add("spring.datasource.url", () -> "jdbc:h2:mem:store;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
			registry.add("spring.datasource.username", () -> "sa");
			registry.add("spring.datasource.password", () -> "");
			registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
		}
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
