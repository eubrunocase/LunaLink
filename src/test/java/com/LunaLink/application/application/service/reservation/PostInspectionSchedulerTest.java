package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostInspectionSchedulerTest {

    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private WebPushService webPushService;

    @InjectMocks
    private PostInspectionScheduler scheduler;

    private Users employee;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        employee = new Users("Funcionario", "101", "func@email.com", "pass", UserRoles.EMPLOYEE);
        employee.setId(UUID.randomUUID());

        Space space = new Space();
        space.setId(1L);
        space.setType(SpaceType.SALAO_FESTAS);

        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setDate(LocalDate.now().minusDays(1));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSpace(space);
    }

    @Test
    @DisplayName("Deve notificar funcionários sobre vistoria pós-evento pendente")
    void checkPostEventInspections_ShouldNotify_WhenReservationsFound() {
        when(reservationRepository.findByDateAndStatusAndSpaceTypes(
                eq(LocalDate.now().minusDays(1)),
                eq(ReservationStatus.CONFIRMED),
                anyList()
        )).thenReturn(List.of(reservation));

        when(userRepository.findByRole(UserRoles.EMPLOYEE)).thenReturn(List.of(employee));

        scheduler.checkPostEventInspections();

        String destination = "/topic/notifications/" + employee.getId();
        verify(messagingTemplate).convertAndSend(eq(destination), any(Object.class));
        verify(webPushService).sendPushNotificationToUser(eq(employee), any());
    }

    @Test
    @DisplayName("Não deve notificar quando não há reservas")
    void checkPostEventInspections_ShouldNotNotify_WhenNoReservations() {
        when(reservationRepository.findByDateAndStatusAndSpaceTypes(
                eq(LocalDate.now().minusDays(1)),
                eq(ReservationStatus.CONFIRMED),
                anyList()
        )).thenReturn(List.of());

        scheduler.checkPostEventInspections();

        verifyNoInteractions(messagingTemplate);
        verifyNoInteractions(webPushService);
    }
}
