package com.cnewbywa.item.service;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@InjectMocks
	private ItemService itemService;
	
	@Mock
	private ItemRepository itemRepository;
	
	@Test
	void testGetItem_Success() {
		Item dbItem = Item.builder().id(1L).build();
		
		Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(dbItem));
		
		Item returnedItem = itemService.getItem(1L);
		
		Assertions.assertNotNull(returnedItem);
		Assertions.assertEquals(1L, returnedItem.getId());
			
		Mockito.verify(itemRepository).findById(1L);
	}
	
	@Test
	void testGetItem_Failute() {
		Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());
		
		Assertions.assertThrows(ItemNotFoundException.class, () -> {
			itemService.getItem(1L);
		});
		
		Mockito.verify(itemRepository).findById(1L);
	}
	
	@Test
	void testGetItems_Success() {
		Item dbItem = Item.builder().id(1L).build();
		Item dbItem2 = Item.builder().id(2L).build();
		
		Mockito.when(itemRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>(Arrays.asList(dbItem, dbItem2))));
		
		Page<Item> returnedPage = itemService.getItems(PageRequest.of(0, 5, Sort.Direction.ASC, "id"));
		
		Assertions.assertNotNull(returnedPage);
		Assertions.assertEquals(2, returnedPage.getSize());
		
		Mockito.verify(itemRepository).findAll(Mockito.any(Pageable.class));
	}
	
	@Test
	void testGetItems_Success_NoResult() {
		Mockito.when(itemRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));
		
		Page<Item> returnedPage = itemService.getItems(PageRequest.of(0, 5, Sort.Direction.ASC, "id"));
		
		Assertions.assertNotNull(returnedPage);
		Assertions.assertEquals(0, returnedPage.getSize());
		
		Mockito.verify(itemRepository).findAll(Mockito.any(Pageable.class));
	}
	
	@Test
	void testAddItem_Success() {
		Item item = Item.builder().id(5L).name("Test item").build();
		
		itemService.addItem(item);
		
		Mockito.verify(itemRepository).save(item);
	}
	
	@Test
	void testUpdateItem_Success() {
		Item item = Item.builder().id(5L).name("Test item").build();
		
		itemService.updateItem(item);
		
		Mockito.verify(itemRepository).save(item);
	}
	
	@Test
	void testDeleteItem_Success() {
		itemService.deleteItem(1L);
		
		Mockito.verify(itemRepository).deleteById(1L);
	}
}
