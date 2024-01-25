package com.cnewbywa.item.service;

import java.util.UUID;

import com.cnewbywa.item.model.ItemAction;

public interface EventSender {

	/**
	 * Send events
	 * 
	 * @param id
	 * @param action
	 * @param eventMessage
	 */
	void sendEvent(UUID id, ItemAction action, String eventMessage);
}
