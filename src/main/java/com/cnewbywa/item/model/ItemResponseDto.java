package com.cnewbywa.item.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ItemResponseDto {

	private long id;
	private String name;
	private String description;
	private Instant createTime;
	private Instant updateTime;
}
