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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.DockerClientFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductServiceIT {

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

		Page<Product> page = productService.findAll(PageRequest.of(0, 10));
		List<ProductDTO> dtos = productMapper.productsToProductDTOs(page.getContent());

		assertThat(dtos).hasSize(1);
		ProductDTO dto = dtos.get(0);
		assertThat(dto.getDescription()).isEqualTo("Attached Product");
		assertThat(dto.getOrderIds()).contains(order.getId());
	}

}