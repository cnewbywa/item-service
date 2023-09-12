package com.cnewbywa.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cnewbywa.item.error.ItemNotFoundException;
import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.repository.ItemRepository;

@Service
@Transactional
public class ItemService {

	@Autowired
	private ItemRepository itemRepository;
	
	public Item getItem(Long id) {
		return itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
	}
	
	public Page<Item> getItems(Pageable pageable) {
		Page<Item> page = itemRepository.findAll(pageable);
		
		return page;
	}
	
	public void addItem(Item item) {
		itemRepository.save(item);
	}
	
	public void updateItem(Item item) {
		itemRepository.save(item);
	}
	
	public void deleteItem(Long id) {
		itemRepository.deleteById(id);
	}
}
