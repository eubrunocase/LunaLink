package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.reservationEvents.*;
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
    private final WebPushService webPushService;

    public ReservationEventListener(UserRepositoryPort repository, SimpMessagingTemplate messagingTemplate, WebPushService webPushService) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
        this.webPushService = webPushService;
    }

    @Async
    @EventListener
    public void handleReservationRequestedEvent(ReservationRequestedEvent event) {
        List<Users> admins = repository.findByRole(UserRoles.ADMIN_ROLE);
        for (Users admin : admins) {
            NotificationDTO notification = new NotificationDTO(
                    "Nova Reserva Pendente",
                    "O residente com ID " + event.getUserId() +
                             " solicitou reserva do espaço " + event.getSpace().getType() +
                             " na data " + event.getDate(),
                    "RESERVATION_REQUESTED",
                    LocalDateTime.now()
            );
            
            String destination = "/topic/notifications/" + admin.getId();
            messagingTemplate.convertAndSend(destination, notification);
            webPushService.sendPushNotificationToUser(admin, notification);
        }
    }

    @Async
    @EventListener
    public void handleReservationApprovedEvent(ReservationApprovedEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            Users resident = residentOpt.get();

            NotificationDTO notificationToAdmin = new NotificationDTO(
                    "Reserva Aprovada",
                    "A reserva do espaço " + event.getSpace().getType() +
                            " para o dia " + event.getDate() + " foi aprovada.",
                    "RESERVATION_APPROVED",
                    LocalDateTime.now()
            );

            List<Users> admins = repository.findByRole(UserRoles.ADMIN_ROLE);
            for (Users admin : admins) {
                String destination = "/topic/notifications/" + admin.getId();
                messagingTemplate.convertAndSend(destination, notificationToAdmin);
                webPushService.sendPushNotificationToUser(admin, notificationToAdmin);
            }

            NotificationDTO notificationToResident = new NotificationDTO(
                    "Reserva Aprovada!",
                    "Sua reserva para o dia " + event.getDate() +
                            " foi aprovada. Aguardando vistoria.",
                    "RESERVATION_APPROVED",
                    LocalDateTime.now()
            );

            String destination = "/topic/notifications/" + resident.getId();
            messagingTemplate.convertAndSend(destination, notificationToResident);
            webPushService.sendPushNotificationToUser(resident, notificationToResident);
        }
    }

    @Async
    @EventListener
    public void handleReservationAwaitingInspectionEvent(ReservationAwaitingInspectionEvent event) {
        List<Users> employees = repository.findByRole(UserRoles.EMPLOYEE);
        for (Users employee : employees) {
            NotificationDTO notification = new NotificationDTO(
                    "Vistoria Necessária",
                    "A reserva do espaço " + event.getSpace().getType() +
                            " para o dia " + event.getDate() +
                            " foi aprovada e aguarda vistoria pré-evento.",
                    "RESERVATION_AWAITING_INSPECTION",
                    LocalDateTime.now()
            );

            String destination = "/topic/notifications/" + employee.getId();
            messagingTemplate.convertAndSend(destination, notification);
            webPushService.sendPushNotificationToUser(employee, notification);
        }
    }

    @Async
    @EventListener
    public void handleReservationAwaitingSignatureEvent(ReservationAwaitingSignatureEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            Users resident = residentOpt.get();
            NotificationDTO notification = new NotificationDTO(
                    "Termo de Responsabilidade Disponível",
                    "A vistoria do espaço " + event.getSpace().getType() +
                            " para o dia " + event.getDate() +
                            " foi concluída. Por favor, assine o termo de responsabilidade.",
                    "RESERVATION_AWAITING_SIGNATURE",
                    LocalDateTime.now()
            );

            String destination = "/topic/notifications/" + resident.getId();
            messagingTemplate.convertAndSend(destination, notification);
            webPushService.sendPushNotificationToUser(resident, notification);
        }
    }

    @Async
    @EventListener
    public void handleReservationConfirmedEvent(ReservationConfirmedEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            Users resident = residentOpt.get();
            NotificationDTO notification = new NotificationDTO(
                    "Reserva Confirmada!",
                    "Sua reserva do espaço " + event.getSpace().getType() +
                            " para o dia " + event.getDate() +
                            " foi confirmada com sucesso.",
                    "RESERVATION_CONFIRMED",
                    LocalDateTime.now()
            );

            String destination = "/topic/notifications/" + resident.getId();
            messagingTemplate.convertAndSend(destination, notification);
            webPushService.sendPushNotificationToUser(resident, notification);
        }
    }

    @Async
    @EventListener
    public void handleReservationRejectedEvent(ReservationRejectedEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            Users resident = residentOpt.get();
            NotificationDTO notification = new NotificationDTO(
                    "Reserva Rejeitada!",
                    "Sua reserva para o dia " + event.getDate() + " foi rejeitada.",
                    "RESERVATION_CANCELLED",
                    LocalDateTime.now()
            );
            
            String destination = "/topic/notifications/" + resident.getId();
            messagingTemplate.convertAndSend(destination, notification);
            webPushService.sendPushNotificationToUser(resident, notification);
        }
    }

}
