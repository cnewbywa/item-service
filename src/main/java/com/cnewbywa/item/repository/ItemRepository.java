package com.cnewbywa.item.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnewbywa.item.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
	
	Optional<Item> findByItemId(String itemId);
	
	void deleteByItemId(String itemId);
}
