package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.events.deliveryEvents.DeliveryCreatedEvent;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DeliveryEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebPushService webPushService;
    private final UserRepositoryPort userRepositoryPort;

    public DeliveryEventListener(SimpMessagingTemplate messagingTemplate, WebPushService webPushService, UserRepositoryPort userRepositoryPort) {
        this.messagingTemplate = messagingTemplate;
        this.webPushService = webPushService;
        this.userRepositoryPort = userRepositoryPort;
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

        // 1. WebSocket
        String destination = "/topic/notifications/" + event.getUserId();
        messagingTemplate.convertAndSend(destination, notification);

        // 2. Web Push
        Optional<Users> userOpt = userRepositoryPort.findById(event.getUserId());
        userOpt.ifPresent(user -> webPushService.sendPushNotificationToUser(user, notification));
    }
}
