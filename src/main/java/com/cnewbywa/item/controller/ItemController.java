package com.cnewbywa.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnewbywa.item.model.Item;
import com.cnewbywa.item.service.ItemService;

import jakarta.annotation.Nonnull;

@RestController
public class ItemController {

	@Autowired
	private ItemService itemService;
	
	@GetMapping
	public Item getIrem(@Nonnull Long id) {
		return itemService.getItem(id);
	}
	
	@GetMapping
	public Page<Item> getItems(Pageable pageable) {
		return itemService.getItems(pageable);
	}
	
	@PostMapping
	public void addItem(@Nonnull Item item) {
		itemService.addItem(item);
	}
	
	@PutMapping
	public void updateItem(@Nonnull Item item) {
		itemService.updateItem(item);
	}
	
	@DeleteMapping
	public void deleteItem(@Nonnull Long id) {
		itemService.deleteItem(id);
	}
}
