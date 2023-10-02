package com.cnewbywa.item.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.cnewbywa.item.configuration.SecurityConfig;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.repository.ItemRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource("classpath:application-it-test.properties")
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" }, topics = { "events" })
@Testcontainers
public class ItemControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
    private JwtDecoder jwtDecoder;
	
	@Autowired
	private ItemRepository itemRepository;

	@LocalServerPort
	private int port;

	@Container
	private static GenericContainer postgresqlContainer = new GenericContainer(DockerImageName.parse("postgres:15.4-alpine"))
		.withEnv("POSTGRES_DB", "postgres")
        .withEnv("POSTGRES_USER", "postgres")
        .withEnv("POSTGRES_PASSWORD", "postgres")
        .withExposedPorts(5432);
	
	@Container
	public static GenericContainer redisContainer = new GenericContainer(DockerImageName.parse("redis:7.2.0"))
	    .withExposedPorts(6379);
	
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> String.format("jdbc:postgresql://%s:%s/%s", postgresqlContainer.getHost(), postgresqlContainer.getFirstMappedPort(), "postgres"));
		registry.add("spring.datasource.username", () -> "postgres");
		registry.add("spring.datasource.password", () -> "postgres");
		
		registry.add("spring.data.redis.host", () -> redisContainer.getHost());
		registry.add("spring.data.redis.port", () -> redisContainer.getFirstMappedPort());
	}
	
	private String item1Id = UUID.randomUUID().toString();
	private String item2Id = UUID.randomUUID().toString();
	
	@BeforeEach
	@Transactional
	void setup() {
		itemRepository.save(Item.builder().itemId(item1Id).name("Item 1").description("Description for item 1").build());
	    itemRepository.save(Item.builder().itemId(item2Id).name("Item 2").description("Description for item 2").build());
	}
	
	@AfterEach
	@Transactional
	void destroy() {
		itemRepository.deleteAll();
	}
	
	@Test
	@WithMockUser
	@Order(1)
	void testGetItem() throws Exception {
		mockMvc
			.perform(get("/" + item1Id).secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Item 1"))
			.andExpect(jsonPath("$.description").value("Description for item 1"));
	}
	
	@Test
	@WithMockUser
	@Order(2)
	void testGetItem_InvalidId() throws Exception {
		mockMvc
			.perform(get("/1").secure(true))
			.andExpect(status().isNotFound());
	}
	
	@Test
	@WithMockUser
	@Order(3)
	void testGetItems() throws Exception {
		mockMvc
			.perform(get("").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].name").value("Item 1"))
			.andExpect(jsonPath("$.items[0].description").value("Description for item 1"))
			.andExpect(jsonPath("$.items[1].name").value("Item 2"))
			.andExpect(jsonPath("$.items[1].description").value("Description for item 2"))
			.andExpect(jsonPath("$.amount").value("2"))
			.andExpect(jsonPath("$.totalAmount").value("2"));
	}
	
	@Test
	@WithMockUser
	@Order(4)
	void testAddItem() throws Exception {
		String content = "{ \"name\": \"Item 3\", \"description\": \"Description for item 3\"}";
		
		mockMvc
			.perform(
				post("").secure(true).contentType(MediaType.APPLICATION_JSON).content(content).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("Item 3"))
			.andExpect(jsonPath("$.description").value("Description for item 3"));
		
		assertEquals(3, itemRepository.count());
	}
	
	@Test
	@WithMockUser
	@Order(5)
	void testUpdateItem() throws Exception {
		String content = "{ \"name\": \"Item 2\", \"description\": \"New description for item 2\"}";
		
		mockMvc
			.perform(
				put("/" + item2Id).secure(true).contentType(MediaType.APPLICATION_JSON).content(content).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Item 2"))
			.andExpect(jsonPath("$.description").value("New description for item 2"));
		
		Optional<Item> item = itemRepository.findByItemId(item2Id);
		
		assertTrue(item.isPresent());
		assertEquals("New description for item 2", item.get().getDescription());
	}
	
	@Test
	@WithMockUser
	@Order(6)
	void testUpdateItem_InvalidId() throws Exception {
		String content = "{ \"name\": \"Item 3\", \"description\": \"New description for item 3\"}";
		
		mockMvc
			.perform(
				put("/3").secure(true).contentType(MediaType.APPLICATION_JSON).content(content).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isNotFound());
	}
	
	@Test
	@WithMockUser
	@Order(7)
	void testDeleteItem() throws Exception {
		mockMvc
			.perform(
				delete("/" + item2Id).secure(true).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isOk())
			.andDo(print());
		
		assertTrue(itemRepository.findByItemId(item2Id).isEmpty());
	}
	
	@Test
	@WithMockUser
	@Order(8)
	void testGetItem_WithHttp() throws Exception {
		mockMvc
			.perform(get("/" + item1Id))
			.andExpect(status().is3xxRedirection());
	}
}
