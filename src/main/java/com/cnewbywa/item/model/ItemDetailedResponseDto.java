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
public class ItemDetailedResponseDto implements Serializable {

	private static final long serialVersionUID = 2459818151898933730L;

	private UUID id;
	private String name;
	private String description;
	private Instant createTime;
	private Instant updateTime;
	private String createdBy;
	private String modifiedBy;
}
