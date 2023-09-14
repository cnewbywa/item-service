package com.cnewbywa.item.service;

import org.junit.jupiter.api.Assertions;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.EventMessage;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@InjectMocks
	private ItemService itemService;
	
	@Mock
	private ItemRepository itemRepository;
	
	@Mock
	private EventSender eventSender;
	
	@Test
	void testGetItem_Success() {
		Item dbItem = Item.builder().id(1L).build();
		
		Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(dbItem));
		
		ItemResponseDto returnedItem = itemService.getItem(1L);
		
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
		
		ItemListResponseDto response = itemService.getItems(PageRequest.of(0, 5, Sort.Direction.ASC, "id"));
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(2, response.getAmount());
		
		Mockito.verify(itemRepository).findAll(Mockito.any(Pageable.class));
	}
	
	@Test
	void testGetItems_Success_NoResult() {
		Mockito.when(itemRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));
		
		ItemListResponseDto response = itemService.getItems(PageRequest.of(0, 5, Sort.Direction.ASC, "id"));
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(0, response.getAmount());
		
		Mockito.verify(itemRepository).findAll(Mockito.any(Pageable.class));
	}
	
	@Test
	void testAddItem_Success() {
		String name = "Item 1";
		String description = "Description of item 1";
		
		Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(Item.builder().id(1L).name(name).description(description).createTime(Instant.now()).build());
		
		ItemDto itemDto = new ItemDto("Item 1", "Description of item 1");
		
		ItemResponseDto response = itemService.addItem(itemDto);
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(1L, response.getId());
		Assertions.assertEquals(itemDto.getName(), response.getName());
		Assertions.assertEquals(itemDto.getDescription(), response.getDescription());
		
		Mockito.verify(itemRepository).save(Mockito.any(Item.class));
		Mockito.verify(eventSender).sendEvent(1L, MessageFormat.format(ItemService.EVENT_MESSAGE__ADD, 1L), EventMessage.EventMessageAction.ADD);
	}
	
	@Test
	void testUpdateItem_Success() {
		String name = "Item 1";
		String description = "Description of item 1";
		
		Item item = Item.builder().id(1L).name(name).description(description).build();
		
		Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
		Mockito.when(itemRepository.save(item)).thenReturn(Item.builder().id(1L).name(name).description(description).createTime(Instant.now()).build());
		
		ItemDto itemDto = new ItemDto("Item 1", "Description of item 1");
		
		ItemResponseDto response = itemService.updateItem(1L, itemDto);
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(1L, response.getId());
		Assertions.assertEquals(itemDto.getName(), response.getName());
		Assertions.assertEquals(itemDto.getDescription(), response.getDescription());
		
		Mockito.verify(itemRepository).findById(1L);
		Mockito.verify(itemRepository).save(Mockito.any(Item.class));
		Mockito.verify(eventSender).sendEvent(1L, MessageFormat.format(ItemService.EVENT_MESSAGE__MODIFY, 1L), EventMessage.EventMessageAction.MODIFY);
	}
	
	@Test
	void testUpdateItem_Failure_IncorrectId() {
		Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());
		
		Assertions.assertThrows(ItemNotFoundException.class, () -> {
			itemService.updateItem(1L, new ItemDto("Item 1", "Description of item 1"));
		});
		
		Mockito.verify(itemRepository).findById(1L);
		Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
		Mockito.verify(eventSender, Mockito.never()).sendEvent(Mockito.anyLong(), Mockito.anyString(), Mockito.any());
	}
	
	@Test
	void testDeleteItem_Success() {
		itemService.deleteItem(1L);
		
		Mockito.verify(itemRepository).deleteById(1L);
		Mockito.verify(eventSender).sendEvent(1L, MessageFormat.format(ItemService.EVENT_MESSAGE__DELETE, 1L), EventMessage.EventMessageAction.DELETE);
	}
}
