package com.cnewbywa.item.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ItemListResponseDto {

	private List<ItemResponseDto> items;
	private long amount;
	private long totalAmount;
}
