package com.cnewbywa.item.service;

import com.cnewbywa.item.model.EventMessage;

public interface EventSender {

	/**
	 * Send events
	 * 
	 * @param id
	 * @param eventMessage
	 * @param action
	 */
	void sendEvent(long id, String eventMessage, EventMessage.EventMessageAction action);
}
