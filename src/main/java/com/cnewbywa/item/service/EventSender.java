package com.cnewbywa.item.service;

import com.cnewbywa.item.model.ItemAction;

public interface EventSender {

	/**
	 * Send events
	 * 
	 * @param id
	 * @param action
	 * @param eventMessage
	 */
	void sendEvent(String id, ItemAction action, String eventMessage);
}
