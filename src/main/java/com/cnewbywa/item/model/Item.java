package com.cnewbywa.item.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@Getter
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Size(min=3, max = 50)
	@NotBlank
	private String name;
	@Size(min=3, max = 50)
	@NotBlank
	private String description;
	@Column(name="create_time", updatable = false)
	private OffsetDateTime createTime;
	@Column(name="update_time")
	private OffsetDateTime updateTime;
}
