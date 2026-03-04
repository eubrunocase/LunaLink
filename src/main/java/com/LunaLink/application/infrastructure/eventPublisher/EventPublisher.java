package com.LunaLink.application.infrastructure.eventPublisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service("customEventPublisher")
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public EventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishEvent(Object event) {
        publisher.publishEvent(event);
    }

}
