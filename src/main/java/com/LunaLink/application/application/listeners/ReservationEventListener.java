package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.reservationEvents.ReservationApprovedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRejectedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ReservationEventListener {

    private final UserRepositoryPort repository;
    private final SimpMessagingTemplate messagingTemplate;

    public ReservationEventListener(UserRepositoryPort repository, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    @EventListener
    public void handleReservationRequestedEvent(ReservationRequestedEvent event) {
        List<Users> admins = repository.findByRole(UserRoles.ADMIN_ROLE);
        for (Users admin : admins) {
            NotificationDTO notification = new NotificationDTO(
                    "Nova Reserva Pendente",
                    "O residente com ID " + event.getUserId() +
                             "solicitou reserva do espaço " + event.getSpace().getType() +
                             "na data " + event.getDate(),
                    "RESERVATION_REQUESTED",
                    LocalDateTime.now()
            );
            String destination = "/topic/notifications/" + admin.getId();
            messagingTemplate.convertAndSend(destination, notification);
        }
    }

    @Async
    @EventListener
    public void handleReservationApprovedEvent(ReservationApprovedEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            NotificationDTO notification = new NotificationDTO(
                    "Reserva Aprovada!",
                    "Sua reserva para o dia " + event.getDate() + " foi aprovada.",
                    "RESERVATION_APPROVED",
                    LocalDateTime.now()
            );
            String destination = "/topic/notifications/" + event.getUserId();
            messagingTemplate.convertAndSend(destination, notification);
            }
    }

    @Async
    @EventListener
    public void handleReservationRejectedEvent(ReservationRejectedEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            NotificationDTO notification = new NotificationDTO(
                    "Reserva Aprovada!",
                    "Sua reserva para o dia " + event.getDate() + " foi rejeitada.",
                    "RESERVATION_CANCELLED",
                    LocalDateTime.now()
            );
            String destination = "/topic/notifications/" + event.getUserId();
            messagingTemplate.convertAndSend(destination, notification);
        }
    }

}
