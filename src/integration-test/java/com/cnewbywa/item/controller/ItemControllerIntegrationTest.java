package com.cnewbywa.item.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.cnewbywa.item.configuration.SecurityConfig;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.repository.ItemRepository;

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
	private static GenericContainer postgresqlContainer = new GenericContainer<>(DockerImageName.parse("postgres:15.4-alpine"))
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
	
	@BeforeEach
	@Transactional
	void setup() {

	}
	
	@AfterEach
	@Transactional
	void destroy() {
		itemRepository.deleteAll();
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItem() throws Exception {
		mockMvc
			.perform(get("/694b64b0-e497-4f15-b481-7ef534c6acf7").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value("694b64b0-e497-4f15-b481-7ef534c6acf7"))
			.andExpect(jsonPath("$.name").value("Item 1"))
			.andExpect(jsonPath("$.description").value("Description for item 1"))
			.andExpect(jsonPath("$.createTime").isNotEmpty())
			.andExpect(jsonPath("$.createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"));
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItem_InvalidId() throws Exception {
		mockMvc
			.perform(get("/1").secure(true))
			.andExpect(status().isNotFound());
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItems() throws Exception {
		mockMvc
			.perform(get("").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value("694b64b0-e497-4f15-b481-7ef534c6acf7"))
			.andExpect(jsonPath("$.items[0].name").value("Item 1"))
			.andExpect(jsonPath("$.items[0].description").value("Description for item 1"))
			.andExpect(jsonPath("$.items[0].createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"))
			.andExpect(jsonPath("$.items[1].id").value("b07cea5b-1420-446c-b463-d5167278575f"))
			.andExpect(jsonPath("$.items[1].name").value("Item 2"))
			.andExpect(jsonPath("$.items[1].description").value("Description for item 2"))
			.andExpect(jsonPath("$.items[1].createdBy").value("9b23b873-f992-471d-94b8-ca69de02684d"))
			.andExpect(jsonPath("$.items[2].id").value("d017c380-d1a9-43db-844d-3592d1d54314"))
			.andExpect(jsonPath("$.items[2].name").value("Item 3"))
			.andExpect(jsonPath("$.items[2].description").value("Description for item 3"))
			.andExpect(jsonPath("$.items[2].createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"))
			.andExpect(jsonPath("$.items[3].id").value("20ba68c6-06db-4aad-b86f-859f68d0324b"))
			.andExpect(jsonPath("$.items[3].name").value("Item 4"))
			.andExpect(jsonPath("$.items[3].description").value("Description for item 4"))
			.andExpect(jsonPath("$.items[3].createdBy").value("user"))
			.andExpect(jsonPath("$.amount").value("4"))
			.andExpect(jsonPath("$.totalAmount").value("4"));
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItemsWithPagination() throws Exception {
		mockMvc
			.perform(get("/?page=0&size=2").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value("694b64b0-e497-4f15-b481-7ef534c6acf7"))
			.andExpect(jsonPath("$.items[0].name").value("Item 1"))
			.andExpect(jsonPath("$.items[0].description").value("Description for item 1"))
			.andExpect(jsonPath("$.items[0].createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"))
			.andExpect(jsonPath("$.items[1].id").value("b07cea5b-1420-446c-b463-d5167278575f"))
			.andExpect(jsonPath("$.items[1].name").value("Item 2"))
			.andExpect(jsonPath("$.items[1].description").value("Description for item 2"))
			.andExpect(jsonPath("$.items[1].createdBy").value("9b23b873-f992-471d-94b8-ca69de02684d"))
			.andExpect(jsonPath("$.amount").value("2"))
			.andExpect(jsonPath("$.totalAmount").value("4"));
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItemsWithPaginationAndSorting() throws Exception {
		mockMvc
			.perform(get("/?page=0&size=3&sort=name,desc").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value("20ba68c6-06db-4aad-b86f-859f68d0324b"))
			.andExpect(jsonPath("$.items[0].name").value("Item 4"))
			.andExpect(jsonPath("$.items[0].description").value("Description for item 4"))
			.andExpect(jsonPath("$.items[0].createdBy").value("user"))
			.andExpect(jsonPath("$.items[1].id").value("d017c380-d1a9-43db-844d-3592d1d54314"))
			.andExpect(jsonPath("$.items[1].name").value("Item 3"))
			.andExpect(jsonPath("$.items[1].description").value("Description for item 3"))
			.andExpect(jsonPath("$.items[1].createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"))
			.andExpect(jsonPath("$.items[2].id").value("b07cea5b-1420-446c-b463-d5167278575f"))
			.andExpect(jsonPath("$.items[2].name").value("Item 2"))
			.andExpect(jsonPath("$.items[2].description").value("Description for item 2"))
			.andExpect(jsonPath("$.items[2].createdBy").value("9b23b873-f992-471d-94b8-ca69de02684d"))
			.andExpect(jsonPath("$.amount").value("3"))
			.andExpect(jsonPath("$.totalAmount").value("4"));
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItemsByUser() throws Exception {
		mockMvc
			.perform(get("/user/38fe0f89-4b11-4e49-b4e6-82ec437f201f").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value("694b64b0-e497-4f15-b481-7ef534c6acf7"))
			.andExpect(jsonPath("$.items[0].name").value("Item 1"))
			.andExpect(jsonPath("$.items[0].description").value("Description for item 1"))
			.andExpect(jsonPath("$.items[0].createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"))
			.andExpect(jsonPath("$.items[1].id").value("d017c380-d1a9-43db-844d-3592d1d54314"))
			.andExpect(jsonPath("$.items[1].name").value("Item 3"))
			.andExpect(jsonPath("$.items[1].description").value("Description for item 3"))
			.andExpect(jsonPath("$.items[1].createdBy").value("38fe0f89-4b11-4e49-b4e6-82ec437f201f"))
			.andExpect(jsonPath("$.amount").value("2"))
			.andExpect(jsonPath("$.totalAmount").value("2"));
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItemsByLoggedInUser() throws Exception {
		mockMvc
			.perform(get("/user").secure(true))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value("20ba68c6-06db-4aad-b86f-859f68d0324b"))
			.andExpect(jsonPath("$.items[0].name").value("Item 4"))
			.andExpect(jsonPath("$.items[0].description").value("Description for item 4"))
			.andExpect(jsonPath("$.items[0].createdBy").value("user"))
			.andExpect(jsonPath("$.amount").value("1"))
			.andExpect(jsonPath("$.totalAmount").value("1"));
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testAddItem() throws Exception {
		String content = "{ \"name\": \"Item 3\", \"description\": \"Description for item 3\"}";
		
		mockMvc
			.perform(
				post("").secure(true).contentType(MediaType.APPLICATION_JSON).content(content).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.name").value("Item 3"))
			.andExpect(jsonPath("$.description").value("Description for item 3"))
			.andExpect(jsonPath("$.createTime").isNotEmpty())
			.andExpect(jsonPath("$.createdBy").value("user"));
		
		assertEquals(5, itemRepository.count());
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testUpdateItem() throws Exception {
		String content = "{ \"name\": \"Item 2\", \"description\": \"New description for item 2\"}";
		
		mockMvc
			.perform(
				put("/b07cea5b-1420-446c-b463-d5167278575f").secure(true).contentType(MediaType.APPLICATION_JSON).content(content).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value("b07cea5b-1420-446c-b463-d5167278575f"))
			.andExpect(jsonPath("$.description").value("New description for item 2"));
		
		Optional<Item> item = itemRepository.findByItemId("b07cea5b-1420-446c-b463-d5167278575f");
		
		assertTrue(item.isPresent());
		assertEquals("New description for item 2", item.get().getDescription());
		assertNotNull(item.get().getUpdateTime());
		assertEquals("user", item.get().getModifiedBy());
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testUpdateItem_InvalidId() throws Exception {
		String content = "{ \"name\": \"Item 3\", \"description\": \"New description for item 3\"}";
		
		mockMvc
			.perform(
				put("/3").secure(true).contentType(MediaType.APPLICATION_JSON).content(content).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isNotFound());
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testDeleteItem() throws Exception {
		mockMvc
			.perform(
				delete("/b07cea5b-1420-446c-b463-d5167278575f").secure(true).with(SecurityMockMvcRequestPostProcessors.jwt()))
			.andExpect(status().isOk())
			.andDo(print());
		
		assertTrue(itemRepository.findByItemId("b07cea5b-1420-446c-b463-d5167278575f").isEmpty());
	}
	
	@Test
	@WithMockUser
	@Sql({"classpath:test_data.sql"})
	void testGetItem_WithHttp() throws Exception {
		mockMvc
			.perform(get("/694b64b0-e497-4f15-b481-7ef534c6acf7"))
			.andExpect(status().is3xxRedirection());
	}
}
