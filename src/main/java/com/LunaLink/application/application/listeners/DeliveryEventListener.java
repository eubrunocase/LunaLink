package com.LunaLink.application.application.listeners;

import com.LunaLink.application.domain.events.deliveryEvents.DeliveryCreatedEvent;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DeliveryEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public DeliveryEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    @EventListener
    public void handleDeliveryCreatedEvent(DeliveryCreatedEvent event) {
        NotificationDTO notification = new NotificationDTO(
                "Nova Encomenda Chegou!",
                "Uma nova encomenda com protocolo " + event.getProtocolNumber() + " foi registrada para você.",
                "DELIVERY_CREATED",
                LocalDateTime.now()
        );

        String destination = "/topic/notifications/" + event.getUserId();
        messagingTemplate.convertAndSend(destination, notification);
    }
}
