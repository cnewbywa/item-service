package com.cnewbywa.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.service.ItemService;

import jakarta.annotation.Nonnull;

@RestController
public class ItemController {

	@Autowired
	private ItemService itemService;
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemResponseDto> getIrem(@PathVariable Long id) {
		return ResponseEntity.ok(itemService.getItem(id));
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemListResponseDto> getItems(Pageable pageable) {
		return ResponseEntity.ok(itemService.getItems(pageable));
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ItemResponseDto> addItem(@RequestBody @Nonnull ItemDto item) {
		return new ResponseEntity<>(itemService.addItem(item), HttpStatus.CREATED);
	}
	
	@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @RequestBody @Nonnull ItemDto item) {
		return ResponseEntity.ok(itemService.updateItem(id, item));
	}
	
	@DeleteMapping(path = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteItem(@PathVariable Long id) {
		itemService.deleteItem(id);
	}
}
