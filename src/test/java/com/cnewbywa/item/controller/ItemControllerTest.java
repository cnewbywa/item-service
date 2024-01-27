package com.cnewbywa.item.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.ItemDetailedResponseDto;
import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.service.ItemService;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

	@Mock
	private ItemService itemService;
	
	@Mock
	private ServletUriComponentsBuilder servletUriComponentsBuilder;
	
	@InjectMocks
	private ItemController itemController;
	
	private UUID item1Id = UUID.randomUUID();
	private UUID item2Id = UUID.randomUUID();
	
	@Test
	void testGetItem_Success() {
		ItemDetailedResponseDto response = ItemDetailedResponseDto.builder().id(item1Id).name("Item 1").description("Description for item 1").build();
		
		Mockito.when(itemService.getItem(item1Id)).thenReturn(response);
		
		ResponseEntity<ItemDetailedResponseDto> responseEntity = itemController.getIrem(item1Id);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		assertDetailedResponseDto(response, responseEntity.getBody());
		
		Mockito.verify(itemService).getItem(item1Id);
	}
	
	@Test
	void testGetItem_Failure() {
		Mockito.when(itemService.getItem(item1Id)).thenThrow(ItemNotFoundException.class);
		
		Assertions.assertThrows(ItemNotFoundException.class, () -> {
			itemService.getItem(item1Id);
	    });

		
		Mockito.verify(itemService).getItem(item1Id);
	}
	
	@Test
	void testGetItems_Success() {
		Pageable pageable = PageRequest.of(0, 2);
		
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 1").createTime(Instant.now()).build();
		ItemResponseDto response2 = ItemResponseDto.builder().id(item2Id).name("Item 2").createTime(Instant.now()).build();
		
		ItemListResponseDto listResponse = ItemListResponseDto.builder().items(Arrays.asList(response, response2)).amount(2).totalAmount(2).build();
		
		Mockito.when(itemService.getItems(pageable)).thenReturn(listResponse);
		
		ResponseEntity<ItemListResponseDto> responseEntity = itemController.getItems(pageable);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		Assertions.assertEquals(2L, responseEntity.getBody().getAmount());
		Assertions.assertEquals(2L, responseEntity.getBody().getTotalAmount());
		assertResponseDto(response, responseEntity.getBody().getItems().get(0));
		assertResponseDto(response2, responseEntity.getBody().getItems().get(1));
		
		Mockito.verify(itemService).getItems(pageable);
	}
	
	@Test
	void testGetItems_Success_NoResult() {
		Pageable pageable = PageRequest.of(0, 2);
		
		ItemListResponseDto listResponse = ItemListResponseDto.builder().items(new ArrayList<>()).amount(0).totalAmount(0).build();
		
		Mockito.when(itemService.getItems(pageable)).thenReturn(listResponse);
		
		ResponseEntity<ItemListResponseDto> responseEntity = itemController.getItems(pageable);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		Assertions.assertEquals(0L, responseEntity.getBody().getAmount());
		Assertions.assertEquals(0L, responseEntity.getBody().getTotalAmount());
		Assertions.assertTrue(responseEntity.getBody().getItems().isEmpty());
		
		Mockito.verify(itemService).getItems(pageable);
	}
	
	@Test
	void testGetItemsByUser_Success() {
		Pageable pageable = PageRequest.of(0, 2);
		
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 1").createTime(Instant.now()).createdBy("test-user-id").build();
		ItemResponseDto response2 = ItemResponseDto.builder().id(item2Id).name("Item 2").createTime(Instant.now()).createdBy("test-user-id").build();
		
		ItemListResponseDto listResponse = ItemListResponseDto.builder().items(Arrays.asList(response, response2)).amount(2).totalAmount(2).build();
		
		Mockito.when(itemService.getItemsByUser("test-user-id", pageable)).thenReturn(listResponse);
		
		ResponseEntity<ItemListResponseDto> responseEntity = itemController.getItemsByUser("test-user-id", pageable);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		Assertions.assertEquals(2L, responseEntity.getBody().getAmount());
		Assertions.assertEquals(2L, responseEntity.getBody().getTotalAmount());
		assertResponseDto(response, responseEntity.getBody().getItems().get(0));
		assertResponseDto(response2, responseEntity.getBody().getItems().get(1));
		
		Mockito.verify(itemService).getItemsByUser("test-user-id", pageable);
	}
	
	@Test
	void testGetItemsByUser_Success_NoResult() {
		Pageable pageable = PageRequest.of(0, 2);
		
		ItemListResponseDto listResponse = ItemListResponseDto.builder().items(new ArrayList<>()).amount(0).totalAmount(0).build();
		
		Mockito.when(itemService.getItemsByUser("test-user-id", pageable)).thenReturn(listResponse);
		
		ResponseEntity<ItemListResponseDto> responseEntity = itemController.getItemsByUser("test-user-id", pageable);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		Assertions.assertEquals(0L, responseEntity.getBody().getAmount());
		Assertions.assertEquals(0L, responseEntity.getBody().getTotalAmount());
		Assertions.assertTrue(responseEntity.getBody().getItems().isEmpty());
		
		Mockito.verify(itemService).getItemsByUser("test-user-id", pageable);
	}
	
	@Test
	void testGetItemsByLoggedInUser_Success() {
		Pageable pageable = PageRequest.of(0, 2);
		
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 1").createTime(Instant.now()).createdBy("test-user-id").build();
		ItemResponseDto response2 = ItemResponseDto.builder().id(item2Id).name("Item 2").createTime(Instant.now()).createdBy("test-user-id").build();
		
		ItemListResponseDto listResponse = ItemListResponseDto.builder().items(Arrays.asList(response, response2)).amount(2).totalAmount(2).build();
		
		Mockito.when(itemService.getItemsByUser("test-user-id", pageable)).thenReturn(listResponse);
		
		ResponseEntity<ItemListResponseDto> responseEntity = itemController.getItemsByLoggedInUser(createAuthentication("test-user-id"), pageable);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		Assertions.assertEquals(2L, responseEntity.getBody().getAmount());
		Assertions.assertEquals(2L, responseEntity.getBody().getTotalAmount());
		assertResponseDto(response, responseEntity.getBody().getItems().get(0));
		assertResponseDto(response2, responseEntity.getBody().getItems().get(1));
		
		Mockito.verify(itemService).getItemsByUser("test-user-id", pageable);
	}
	
	@Test
	void testGetItemsByLoggedInUser_Success_NoResult() {
		Pageable pageable = PageRequest.of(0, 2);
		
		ItemListResponseDto listResponse = ItemListResponseDto.builder().items(new ArrayList<>()).amount(0).totalAmount(0).build();
		
		Mockito.when(itemService.getItemsByUser("test-user-id", pageable)).thenReturn(listResponse);
		
		ResponseEntity<ItemListResponseDto> responseEntity = itemController.getItemsByLoggedInUser(createAuthentication("test-user-id"), pageable);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		Assertions.assertEquals(0L, responseEntity.getBody().getAmount());
		Assertions.assertEquals(0L, responseEntity.getBody().getTotalAmount());
		Assertions.assertTrue(responseEntity.getBody().getItems().isEmpty());
		
		Mockito.verify(itemService).getItemsByUser("test-user-id", pageable);
	}
	
	@Test
	void testAddItem() {
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        
		ItemDetailedResponseDto response = ItemDetailedResponseDto.builder().id(item1Id).name("Item 2").description("Description for item 2").build();
		
		ItemDto input = new ItemDto("Item 2", "Description for item 2");
		
		Mockito.when(itemService.addItem(input, "test-user-id")).thenReturn(response);
		
		ResponseEntity<ItemDetailedResponseDto> responseEntity = itemController.addItem(createAuthentication("test-user-id"), input);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		assertDetailedResponseDto(response, responseEntity.getBody());
		
		Mockito.verify(itemService).addItem(input, "test-user-id");
	}
	
	@Test
	void testAddItem_NoLoggedInUser() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.addItem(null, null);
	    });
		
		Mockito.verify(itemService, Mockito.never()).addItem(Mockito.any(ItemDto.class), Mockito.anyString());
	}
	
	@Test
	void testAddItem_NoLoggedInUser2() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.addItem(createAuthentication(null), null);
	    });
		
		Mockito.verify(itemService, Mockito.never()).addItem(Mockito.any(ItemDto.class), Mockito.anyString());
	}
	
	@Test
	void testUpdateItem() {
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		
		ItemDetailedResponseDto response = ItemDetailedResponseDto.builder().id(item1Id).name("Item 2").description("New description for item 2").build();
		
		ItemDto input = new ItemDto("Item 2", "New description for item 2");
		
		Mockito.when(itemService.updateItem(item2Id, input, "test-user-id")).thenReturn(response);
		
		ResponseEntity<ItemDetailedResponseDto> responseEntity = itemController.updateItem(createAuthentication("test-user-id"), item2Id, input);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		assertDetailedResponseDto(response, responseEntity.getBody());
		
		Mockito.verify(itemService).updateItem(item2Id, input, "test-user-id");
	}
	
	@Test
	void testUpdateItem_NoLoggedInUser() {
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.updateItem(null, item1Id, null);
	    });
		
		Mockito.verify(itemService, Mockito.never()).updateItem(Mockito.any(UUID.class), Mockito.any(ItemDto.class), Mockito.anyString());
	}
	
	@Test
	void testUpdateItem_NoLoggedInUser2() {
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.updateItem(createAuthentication(null), item1Id, null);
	    });
		
		Mockito.verify(itemService, Mockito.never()).updateItem(Mockito.any(UUID.class), Mockito.any(ItemDto.class), Mockito.anyString());
	}
	
	@Test
	void testDeleteItem() {
		Mockito.doNothing().when(itemService).deleteItem(item2Id, "test-user-id");
		
		itemController.deleteItem(createAuthentication("test-user-id"), item2Id);
		
		Mockito.verify(itemService).deleteItem(item2Id, "test-user-id");
	}
	
	@Test
	void testDeleteItem_NoLoggedInUser() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.deleteItem(null, item1Id);
	    });
		
		Mockito.verify(itemService, Mockito.never()).deleteItem(Mockito.any(UUID.class), Mockito.anyString());
	}
	
	@Test
	void testDeleteItem_NoLoggedInUser2() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.deleteItem(createAuthentication(null), item1Id);
	    });
		
		Mockito.verify(itemService, Mockito.never()).deleteItem(Mockito.any(UUID.class), Mockito.anyString());
	}
	
	private void assertDetailedResponseDto(ItemDetailedResponseDto expectedResponse, ItemDetailedResponseDto actualResponse) {
		Assertions.assertEquals(expectedResponse.getId(), actualResponse.getId());
		Assertions.assertEquals(expectedResponse.getName(), actualResponse.getName());
		Assertions.assertEquals(expectedResponse.getDescription(), actualResponse.getDescription());
	}
	
	private void assertResponseDto(ItemResponseDto expectedResponse, ItemResponseDto actualResponse) {
		Assertions.assertEquals(expectedResponse.getId(), actualResponse.getId());
		Assertions.assertEquals(expectedResponse.getName(), actualResponse.getName());
		Assertions.assertNotNull(actualResponse.getCreateTime());
	}
	
	private Authentication createAuthentication(String user) {
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "HS256");
		headers.put("typ", "JWT");
		
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", user);
		
		Jwt jwt = new Jwt("not empty", Instant.now(), Instant.now().plusSeconds(60), headers, claims);
		
		return new JwtAuthenticationToken(jwt, null, user);
	}
}
