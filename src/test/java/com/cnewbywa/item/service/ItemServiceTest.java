package com.cnewbywa.item.service;

import org.junit.jupiter.api.Assertions;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

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
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.model.ItemAction;
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
	
	private String item1Id = UUID.randomUUID().toString();
	private String item2Id = UUID.randomUUID().toString();
	
	@Test
	void testGetItem_Success() {
		Item dbItem = Item.builder().itemId(item1Id).build();
		
		Mockito.when(itemRepository.findByItemId(item1Id)).thenReturn(Optional.of(dbItem));
		
		ItemResponseDto returnedItem = itemService.getItem(item1Id);
		
		Assertions.assertNotNull(returnedItem);
		Assertions.assertEquals(item1Id, returnedItem.getId());
			
		Mockito.verify(itemRepository).findByItemId(item1Id);
	}
	
	@Test
	void testGetItem_Failute() {
		Mockito.when(itemRepository.findByItemId(item1Id)).thenReturn(Optional.empty());
		
		Assertions.assertThrows(ItemNotFoundException.class, () -> {
			itemService.getItem(item1Id);
		});
		
		Mockito.verify(itemRepository).findByItemId(item1Id);
	}
	
	@Test
	void testGetItems_Success() {
		Item dbItem = Item.builder().itemId(item1Id).build();
		Item dbItem2 = Item.builder().itemId(item2Id).build();
		
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
		
		Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(Item.builder().itemId(item1Id).name(name).description(description).createTime(Instant.now()).build());
		
		ItemDto itemDto = new ItemDto("Item 1", "Description of item 1");
		
		ItemResponseDto response = itemService.addItem(itemDto, "user1");
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(item1Id, response.getId());
		Assertions.assertEquals(itemDto.getName(), response.getName());
		Assertions.assertEquals(itemDto.getDescription(), response.getDescription());
		
		Mockito.verify(itemRepository).save(Mockito.any(Item.class));
		Mockito.verify(eventSender).sendEvent(item1Id, ItemAction.ADD, MessageFormat.format(ItemService.EVENT_MESSAGE__ADD, item1Id));
	}
	
	@Test
	void testUpdateItem_Success() {
		String name = "Item 1";
		String description = "Description of item 1";
		
		Item item = Item.builder().itemId(item1Id).name(name).description(description).build();
		
		Mockito.when(itemRepository.findByItemId(item1Id)).thenReturn(Optional.of(item));
		Mockito.when(itemRepository.save(item)).thenReturn(Item.builder().itemId(item1Id).name(name).description(description).createTime(Instant.now()).build());
		
		ItemDto itemDto = new ItemDto("Item 1", "Description of item 1");
		
		ItemResponseDto response = itemService.updateItem(item1Id, itemDto, "user1");
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(item1Id, response.getId());
		Assertions.assertEquals(itemDto.getName(), response.getName());
		Assertions.assertEquals(itemDto.getDescription(), response.getDescription());
		
		Mockito.verify(itemRepository).findByItemId(item1Id);
		Mockito.verify(itemRepository).save(Mockito.any(Item.class));
		Mockito.verify(eventSender).sendEvent(item1Id, ItemAction.MODIFY, MessageFormat.format(ItemService.EVENT_MESSAGE__MODIFY, item1Id));
	}
	
	@Test
	void testUpdateItem_Failure_IncorrectId() {
		Mockito.when(itemRepository.findByItemId(item1Id)).thenReturn(Optional.empty());
		
		Assertions.assertThrows(ItemNotFoundException.class, () -> {
			itemService.updateItem(item1Id, new ItemDto("Item 1", "Description of item 1"), "user1");
		});
		
		Mockito.verify(itemRepository).findByItemId(item1Id);
		Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
		Mockito.verify(eventSender, Mockito.never()).sendEvent(Mockito.anyString(), Mockito.any(), Mockito.anyString());
	}
	
	@Test
	void testDeleteItem_Success() {
		itemService.deleteItem(item1Id, "user1");
		
		Mockito.verify(itemRepository).deleteByItemId(item1Id);
		Mockito.verify(eventSender).sendEvent(item1Id, ItemAction.DELETE, MessageFormat.format(ItemService.EVENT_MESSAGE__DELETE, item1Id));
	}
}
