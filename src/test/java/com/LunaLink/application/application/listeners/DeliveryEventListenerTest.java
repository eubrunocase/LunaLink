package com.LunaLink.application.application.listeners;

import com.LunaLink.application.domain.events.deliveryEvents.DeliveryCreatedEvent;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryEventListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private DeliveryEventListener listener;

    @Test
    @DisplayName("Deve enviar notificação para usuário ao criar entrega")
    void handleDeliveryCreatedEvent_ShouldSendNotification_ToUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DeliveryCreatedEvent event = new DeliveryCreatedEvent(UUID.randomUUID(), userId, "PROTO-123", LocalDateTime.now());

        // Act
        listener.handleDeliveryCreatedEvent(event);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + userId), any(NotificationDTO.class));
    }
}
