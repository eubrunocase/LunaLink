package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostInspectionScheduler {

    private static final List<SpaceType> SPACES_REQUIRING_INSPECTION = List.of(
            SpaceType.SALAO_FESTAS,
            SpaceType.CHURRASQUEIRA
    );

    private final ReservationRepositoryPort reservationRepository;
    private final UserRepositoryPort userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebPushService webPushService;

    public PostInspectionScheduler(ReservationRepositoryPort reservationRepository,
                                   UserRepositoryPort userRepository,
                                   SimpMessagingTemplate messagingTemplate,
                                   WebPushService webPushService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.webPushService = webPushService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void checkPostEventInspections() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Reservation> confirmedReservations = reservationRepository.findByDateAndStatusAndSpaceTypes(
                yesterday,
                ReservationStatus.CONFIRMED,
                SPACES_REQUIRING_INSPECTION
        );

        if (confirmedReservations.isEmpty()) {
            return;
        }

        List<Users> employees = userRepository.findByRole(UserRoles.EMPLOYEE);

        for (Reservation reservation : confirmedReservations) {
            for (Users employee : employees) {
                NotificationDTO notification = new NotificationDTO(
                        "Vistoria Pós-Evento Pendente",
                        "A reserva do espaço " + reservation.getSpace().getType() +
                                " para o dia " + reservation.getDate() +
                                " requer vistoria pós-evento.",
                        "POST_EVENT_INSPECTION_REQUIRED",
                        LocalDateTime.now()
                );

                String destination = "/topic/notifications/" + employee.getId();
                messagingTemplate.convertAndSend(destination, notification);
                webPushService.sendPushNotificationToUser(employee, notification);
            }
        }
    }
}
