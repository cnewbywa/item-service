package com.cnewbywa.item.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.repository.ItemRepository;

@Service
@Transactional
public class ItemService {

	@Autowired
	private ItemRepository itemRepository;
	
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
		
		return createResponseDto(item);
	}
	
	public ItemResponseDto updateItem(long id, ItemDto itemDto) {
		Item dbItem = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
		
		dbItem.setName(itemDto.getName());
		dbItem.setDescription(itemDto.getDescription());
		
		Item item = itemRepository.save(dbItem);
		
		return createResponseDto(item);
	}
	
	public void deleteItem(Long id) {
		itemRepository.deleteById(id);
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
