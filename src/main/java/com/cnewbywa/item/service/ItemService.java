package com.cnewbywa.item.service;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.EventMessage;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.repository.ItemRepository;

@Service
@Transactional
public class ItemService {

	static final String EVENT_MESSAGE__ADD = "Item with id {0} was added";
    static final String EVENT_MESSAGE__MODIFY = "Item with id {0} was modified";
    static final String EVENT_MESSAGE__DELETE = "Item with id {0} was removed";
    
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
    private EventSender eventSender;
	
	public ItemResponseDto getItem(Long id) {
		Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
		
		return createResponseDto(item);
	}
	
	public ItemListResponseDto getItems(Pageable pageable) {
		Page<Item> page = itemRepository.findAll(pageable);
		
		List<ItemResponseDto> responses = page.getContent().stream().map(this::createResponseDto).toList();
		
		return ItemListResponseDto.builder().amount(page.getNumberOfElements()).totalAmount(page.getTotalElements()).items(responses).build();
	}
	
	public ItemResponseDto addItem(ItemDto itemDto) {
		Item item = itemRepository.save(Item.builder().name(itemDto.getName()).description(itemDto.getDescription()).build());
		
		eventSender.sendEvent(item.getId(), MessageFormat.format(EVENT_MESSAGE__ADD, item.getId()), EventMessage.EventMessageAction.ADD);
		
		return createResponseDto(item);
	}
	
	public ItemResponseDto updateItem(long id, ItemDto itemDto) {
		Item dbItem = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
		
		dbItem.setName(itemDto.getName());
		dbItem.setDescription(itemDto.getDescription());
		
		Item item = itemRepository.save(dbItem);
		
		eventSender.sendEvent(id, MessageFormat.format(EVENT_MESSAGE__MODIFY, id), EventMessage.EventMessageAction.MODIFY);
		
		return createResponseDto(item);
	}
	
	public void deleteItem(Long id) {
		itemRepository.deleteById(id);
		
		eventSender.sendEvent(id, MessageFormat.format(EVENT_MESSAGE__DELETE, id), EventMessage.EventMessageAction.DELETE);
	}
	
	private ItemResponseDto createResponseDto(Item item) {
		return ItemResponseDto.builder()
				.id(item.getId())
				.name(item.getName())
				.description(item.getDescription())
				.createTime(item.getCreateTime())
				.updateTime(item.getUpdateTime()).build();
	}
}
