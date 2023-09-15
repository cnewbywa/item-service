package com.cnewbywa.item.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemListResponseDto implements Serializable {

	private static final long serialVersionUID = -5349371588657761319L;
	
	private List<ItemResponseDto> items;
	private long amount;
	private long totalAmount;
}
