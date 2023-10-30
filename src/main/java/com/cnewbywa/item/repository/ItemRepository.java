package com.cnewbywa.item.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cnewbywa.item.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
	
	Optional<Item> findByItemId(String itemId);
	
	Page<Item> findAllByCreatedBy(String user, Pageable pageable);
	
	void deleteByItemId(String itemId);
}
