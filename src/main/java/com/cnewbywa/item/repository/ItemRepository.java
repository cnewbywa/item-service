package com.cnewbywa.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnewbywa.item.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
	
}
