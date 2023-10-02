package com.cnewbywa.item.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventMessage {

	public enum EventMessageAction {
	    ADD, MODIFY, DELETE; 
	}
	
	private String id;
    private EventMessageAction action;
    private String message;
    private final Instant creationTime = Instant.now();
    private final String applicationId;
}
