package com.cnewbywa.item.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cnewbywa.item.model.EventMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventSenderImpl implements EventSender {

	@Value("${application.events.topic}")
    private String eventTopic;
	
	@Value("${application.id}")
    private String applicationId;

    @Autowired
    private KafkaTemplate<String, EventMessage> kafkaTemplate;
	
    @Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEvent(String id, String eventMessage, EventMessage.EventMessageAction action) {
    	sendEvent(EventMessage.builder()
                .message(String.format(eventMessage, id))
                .action(action)
                .id(id)
                .applicationId(applicationId)
                .build());
	}
    
	private void sendEvent(EventMessage eventMessage) {
		log.debug("Sending event to topic {}...", eventTopic);
		
		CompletableFuture<SendResult<String, EventMessage>> kafkaFuture = kafkaTemplate.send(eventTopic, eventMessage);

		kafkaFuture.whenComplete((result, exception) -> {
			if (exception != null) {
				log.error("Error sending event message " + eventMessage, exception);
			} else {
				log.debug("Event message sent successfully");
			}
		});
	}
}
