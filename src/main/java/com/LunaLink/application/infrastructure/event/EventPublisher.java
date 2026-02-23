package com.LunaLink.application.infrastructure.event;

import com.LunaLink.application.domain.events.DeliveryReceivedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public EventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishEvent(DeliveryReceivedEvent event) {
        publisher.publishEvent(event);
    }

}
