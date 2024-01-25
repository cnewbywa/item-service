package com.cnewbywa.item.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.model.ItemAction;
import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.repository.ItemRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ItemService {

	static final String EVENT_MESSAGE__ADD = "Item with id {0} was added";
    static final String EVENT_MESSAGE__MODIFY = "Item with id {0} was modified";
    static final String EVENT_MESSAGE__DELETE = "Item with id {0} was removed";
    
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
    private EventSender eventSender;
	
	@Cacheable("item")
	public ItemResponseDto getItem(UUID id) {
		Item item = itemRepository.findByItemId(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
		
		return createResponseDto(item);
	}
	
	@Cacheable("items")
	public ItemListResponseDto getItems(Pageable pageable) {
		Page<Item> page = itemRepository.findAll(pageable);
		
		List<ItemResponseDto> responses = page.getContent().stream().map(this::createResponseDto).toList();
		
		return ItemListResponseDto.builder().amount(page.getNumberOfElements()).totalAmount(page.getTotalElements()).items(responses).build();
	}
	
	@Cacheable("items")
	public ItemListResponseDto getItemsByUser(String user, Pageable pageable) {
		Page<Item> page = itemRepository.findAllByCreatedBy(user, pageable);
		
		List<ItemResponseDto> responses = page.getContent().stream().map(this::createResponseDto).toList();
		
		return ItemListResponseDto.builder().amount(page.getNumberOfElements()).totalAmount(page.getTotalElements()).items(responses).build();
	}
	
	public ItemResponseDto addItem(ItemDto itemDto, String user) {
		log.debug("User: " + user);
		
		Item item = itemRepository.save(Item.builder().name(itemDto.getName()).description(itemDto.getDescription()).build());
		
		eventSender.sendEvent(item.getItemId(), ItemAction.ADD, MessageFormat.format(EVENT_MESSAGE__ADD, item.getItemId()));
		
		return createResponseDto(item);
	}
	
	@CacheEvict(value = "item", key = "#id")
	public ItemResponseDto updateItem(UUID id, ItemDto itemDto, String user) {
		Item dbItem = itemRepository.findByItemId(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
		
		dbItem.setName(itemDto.getName());
		dbItem.setDescription(itemDto.getDescription());
		
		Item item = itemRepository.save(dbItem);
		
		eventSender.sendEvent(id, ItemAction.MODIFY, MessageFormat.format(EVENT_MESSAGE__MODIFY, id));
		
		return createResponseDto(item);
	}
	
	@CacheEvict(value = "item", key = "#id")
	public void deleteItem(UUID id, String user) {
		itemRepository.deleteByItemId(id);
		
		eventSender.sendEvent(id, ItemAction.DELETE, MessageFormat.format(EVENT_MESSAGE__DELETE, id));
	}
	
	private ItemResponseDto createResponseDto(Item item) {
		return ItemResponseDto.builder()
				.id(item.getItemId())
				.name(item.getName())
				.description(item.getDescription())
				.createTime(item.getCreateTime())
				.updateTime(item.getUpdateTime())
				.createdBy(item.getCreatedBy())
				.modifiedBy(item.getModifiedBy()).build();
	}
}
