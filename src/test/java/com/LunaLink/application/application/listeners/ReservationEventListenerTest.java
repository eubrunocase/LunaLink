package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.reservationEvents.ReservationApprovedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRejectedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationEventListenerTest {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ReservationEventListener listener;

    @Test
    @DisplayName("Deve enviar notificação para admins ao solicitar reserva")
    void handleReservationRequestedEvent_ShouldSendNotification_ToAdmins() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Space space = new Space();
        space.setId(1L);
        ReservationRequestedEvent event = new ReservationRequestedEvent(UUID.randomUUID(), userId, LocalDate.now(), space);
        
        Users admin = new Users("Admin", "101", "admin@email.com", "pass", UserRoles.ADMIN_ROLE);
        admin.setId(UUID.randomUUID());
        
        when(repository.findByRole(UserRoles.ADMIN_ROLE)).thenReturn(List.of(admin));

        // Act
        listener.handleReservationRequestedEvent(event);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + admin.getId()), any(NotificationDTO.class));
    }

    @Test
    @DisplayName("Deve enviar notificação para usuário ao aprovar reserva")
    void handleReservationApprovedEvent_ShouldSendNotification_ToUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Space space = new Space();
        space.setId(1L);
        ReservationApprovedEvent event = new ReservationApprovedEvent(UUID.randomUUID(), userId, LocalDate.now(), space);
        
        Users user = new Users("User", "101", "user@email.com", "pass", UserRoles.RESIDENT_ROLE);
        user.setId(userId);
        
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        listener.handleReservationApprovedEvent(event);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + userId), any(NotificationDTO.class));
    }

    @Test
    @DisplayName("Deve enviar notificação para usuário ao rejeitar reserva")
    void handleReservationRejectedEvent_ShouldSendNotification_ToUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Space space = new Space();
        space.setId(1L);
        ReservationRejectedEvent event = new ReservationRejectedEvent(UUID.randomUUID(), userId, LocalDate.now(), space);
        
        Users user = new Users("User", "101", "user@email.com", "pass", UserRoles.RESIDENT_ROLE);
        user.setId(userId);
        
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        listener.handleReservationRejectedEvent(event);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + userId), any(NotificationDTO.class));
    }
}
