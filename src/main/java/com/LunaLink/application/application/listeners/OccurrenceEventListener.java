package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.occurrenceEvents.OccurrenceCreatedEvent;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OccurrenceEventListener {

    private final UserRepositoryPort userRepositoryPort;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebPushService webPushService;

    public OccurrenceEventListener(UserRepositoryPort userRepositoryPort, SimpMessagingTemplate messagingTemplate, WebPushService webPushService) {
        this.userRepositoryPort = userRepositoryPort;
        this.messagingTemplate = messagingTemplate;
        this.webPushService = webPushService;
    }

    @Async
    @EventListener
    public void handleOccurrenceCreatedEvent(OccurrenceCreatedEvent event) {
        List<Users> admins = userRepositoryPort.findByRole(UserRoles.ADMIN_ROLE);
        for (Users admin : admins) {
            NotificationDTO notification = new NotificationDTO(
                    "Nova Ocorrência Registrada",
                    "Morador: " + event.getUserName() + " relatou: " + event.getDescriptionSnippet(),
                    "OCCURRENCE_CREATED",
                    LocalDateTime.now()
            );

            // 1. WebSocket
            String destination = "/topic/notifications/" + admin.getId();
            messagingTemplate.convertAndSend(destination, notification);

            // 2. Web Push
            webPushService.sendPushNotificationToUser(admin, notification);
        }
    }
}
