package com.LunaLink.application.infrastructure.event;

import com.LunaLink.application.domain.events.DeliveryReceivedEvent;
import com.LunaLink.application.domain.events.ReservationRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public EventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishDeliveryReceivedEvent(DeliveryReceivedEvent event) {
        publisher.publishEvent(event);
    }

    public void publishReservationRequestedEvent(ReservationRequestedEvent event) {
        publisher.publishEvent(event);
    }



}
