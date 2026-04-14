package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.occurrenceEvents.OccurrenceCreatedEvent;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OccurrenceEventListenerTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private WebPushService webPushService;

    @InjectMocks
    private OccurrenceEventListener listener;

    @Test
    @DisplayName("Deve enviar notificação para admins ao criar ocorrencia")
    void handleOccurrenceCreatedEvent_ShouldSendNotification_ToAdmins() {
        // Arrange
        UUID occurrenceId = UUID.randomUUID();
        OccurrenceCreatedEvent event = new OccurrenceCreatedEvent(occurrenceId, "Bruno", "Barulho excessivo");
        
        Users admin = new Users();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRoles.ADMIN_ROLE);
        
        when(userRepositoryPort.findByRole(UserRoles.ADMIN_ROLE)).thenReturn(List.of(admin));

        // Act
        listener.handleOccurrenceCreatedEvent(event);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + admin.getId()), any(NotificationDTO.class));
        verify(webPushService, times(1)).sendPushNotificationToUser(eq(admin), any(NotificationDTO.class));
    }
}
