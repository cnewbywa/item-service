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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.cnewbywa.item.model.ItemDto;
import com.cnewbywa.item.model.ItemListResponseDto;
import com.cnewbywa.item.model.ItemResponseDto;
import com.cnewbywa.item.service.ItemService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

	@Autowired
	private ItemService itemService;
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemResponseDto> getIrem(@PathVariable String id) {
		return ResponseEntity.ok(itemService.getItem(id));
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemListResponseDto> getItems(Pageable pageable) {
		return ResponseEntity.ok(itemService.getItems(pageable));
	}
	
	@GetMapping(path = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemListResponseDto> getItemsByUser(@PathVariable String userId, Pageable pageable) {
		return ResponseEntity.ok(itemService.getItemsByUser(userId, pageable));
	}
	
	@GetMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemListResponseDto> getItemsByLoggedInUser(Authentication authentication, Pageable pageable) {
		return ResponseEntity.ok(itemService.getItemsByUser(getLoggedInUser(authentication), pageable));
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<ItemResponseDto> addItem(Authentication authentication, @RequestBody @Nonnull ItemDto item) {
		return new ResponseEntity<>(itemService.addItem(item, getLoggedInUser(authentication)), HttpStatus.CREATED);
	}
	
	@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ItemResponseDto> updateItem(Authentication authentication, @PathVariable String id, @RequestBody @Nonnull ItemDto item) {
		return ResponseEntity.ok(itemService.updateItem(id, item, getLoggedInUser(authentication)));
	}
	
	@DeleteMapping(path = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteItem(Authentication authentication, @PathVariable String id) {
		itemService.deleteItem(id, getLoggedInUser(authentication));
	}
	
	private String getLoggedInUser(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			log.error("Username cannot be found");
			
			throw new UsernameNotFoundException("User not found");
		}
		
		return authentication.getName();
	}
}
