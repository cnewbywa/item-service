package com.cnewbywa.item.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

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
public class ItemResponseDto implements Serializable {

	private static final long serialVersionUID = 5394128128404678166L;
	
	private UUID id;
	private String name;
	private Instant createTime;
	private String createdBy;
}
