package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.events.announcementEvents.AnnouncementCreatedEvent;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnnouncementEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebPushService webPushService;
    private final UserRepositoryPort userRepositoryPort;

    public AnnouncementEventListener(SimpMessagingTemplate messagingTemplate,
                                     WebPushService webPushService,
                                     UserRepositoryPort userRepositoryPort) {
        this.messagingTemplate = messagingTemplate;
        this.webPushService = webPushService;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Async
    @EventListener
    public void handleAnnouncementCreatedEvent(AnnouncementCreatedEvent announcementCreatedEvent) {
        NotificationDTO notification = new NotificationDTO(
                "Um novo comunicado foi publicado!",
                "O adminstrador publicou um novo comunicado, acesse o app e confira!" + announcementCreatedEvent.getTitle(),
                "ANNOUNCEMENT_CREATED",
                announcementCreatedEvent.getCreatedAt()
        );

        String destination = "/topic/notifications" + announcementCreatedEvent.getAnnouncementId();
        messagingTemplate.convertAndSend(destination, notification);

        List<Users> allUsers = userRepositoryPort.findAll();
        for(Users user : allUsers){
            webPushService.sendPushNotificationToUser(user, notification);
        }
    }

}
