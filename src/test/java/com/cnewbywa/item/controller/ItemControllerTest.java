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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.service.ItemService;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

	@Mock
	private ItemService itemService;
	
	@InjectMocks
	private ItemController itemController;
	
	private String item1Id = UUID.randomUUID().toString();
	private String item2Id = UUID.randomUUID().toString();
	
	@Test
	void testGetItem_Success() {
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 1").description("Description for item 1").build();
		
		Mockito.when(itemService.getItem(item1Id)).thenReturn(response);
		
		ResponseEntity<ItemResponseDto> responseEntity = itemController.getIrem(item1Id);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		assertResponseDto(response, responseEntity.getBody());
		
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
		
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 1").description("Description for item 1").build();
		ItemResponseDto response2 = ItemResponseDto.builder().id(item2Id).name("Item 2").description("Description for item 2").build();
		
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
	void testAddItem() {
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 2").description("Description for item 2").build();
		
		ItemDto input = new ItemDto("Item 2", "Description for item 2");
		
		Mockito.when(itemService.addItem(input, "test-user-id")).thenReturn(response);
		
		ResponseEntity<ItemResponseDto> responseEntity = itemController.addItem(createAuthentication("test-user-id"), input);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		assertResponseDto(response, responseEntity.getBody());
		
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
		ItemResponseDto response = ItemResponseDto.builder().id(item1Id).name("Item 2").description("New description for item 2").build();
		
		ItemDto input = new ItemDto("Item 2", "New description for item 2");
		
		Mockito.when(itemService.updateItem(item2Id, input, "test-user-id")).thenReturn(response);
		
		ResponseEntity<ItemResponseDto> responseEntity = itemController.updateItem(createAuthentication("test-user-id"), item2Id, input);
		
		Assertions.assertNotNull(responseEntity);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertNotNull(responseEntity.getBody());
		assertResponseDto(response, responseEntity.getBody());
		
		Mockito.verify(itemService).updateItem(item2Id, input, "test-user-id");
	}
	
	@Test
	void testUpdateItem_NoLoggedInUser() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.updateItem(null, item1Id, null);
	    });
		
		Mockito.verify(itemService, Mockito.never()).updateItem(Mockito.anyString(), Mockito.any(ItemDto.class), Mockito.anyString());
	}
	
	@Test
	void testUpdateItem_NoLoggedInUser2() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.updateItem(createAuthentication(null), item1Id, null);
	    });
		
		Mockito.verify(itemService, Mockito.never()).updateItem(Mockito.anyString(), Mockito.any(ItemDto.class), Mockito.anyString());
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
		
		Mockito.verify(itemService, Mockito.never()).deleteItem(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	void testDeleteItem_NoLoggedInUser2() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			itemController.deleteItem(createAuthentication(null), item1Id);
	    });
		
		Mockito.verify(itemService, Mockito.never()).deleteItem(Mockito.anyString(), Mockito.anyString());
	}
	
	private void assertResponseDto(ItemResponseDto expectedResponse, ItemResponseDto actualResponse) {
		Assertions.assertEquals(expectedResponse.getId(), actualResponse.getId());
		Assertions.assertEquals(expectedResponse.getName(), actualResponse.getName());
		Assertions.assertEquals(expectedResponse.getDescription(), actualResponse.getDescription());
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
