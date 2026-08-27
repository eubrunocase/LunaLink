package com.LunaLink.application.infrastructure.repository.reservation;

import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Autowired
    private EntityManager entityManager;

    private Users user;
    private Space salao;
    private Space churrasqueira;
    private Space academia;

    @BeforeEach
    void setUp() {
        user = new Users("Maria Silva", "102", "maria@test.com", "pass", UserRoles.RESIDENT_ROLE);
        entityManager.persist(user);

        salao = new Space();
        salao.setType(SpaceType.SALAO_FESTAS);
        entityManager.persist(salao);

        churrasqueira = new Space();
        churrasqueira.setType(SpaceType.CHURRASQUEIRA);
        entityManager.persist(churrasqueira);

        academia = new Space();
        academia.setType(SpaceType.ACADEMIA);
        entityManager.persist(academia);

        entityManager.flush();
    }

    private Reservation createReservation(int day, Space space, ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setDate(LocalDate.of(2026, 5, day));
        reservation.setStatus(status);
        reservation.assignTo(user, space);
        reservation.setCreatedAt(java.time.LocalDateTime.of(2026, 5, day, 10, 0));
        entityManager.persist(reservation);
        return reservation;
    }

    @Test
    @DisplayName("Deve filtrar por status confirmado e espaços tarifados")
    void findReservationsForReport_shouldFilterByStatusAndSpaceTypes() {
        Reservation aprovadaSalao = createReservation(10, salao, ReservationStatus.CONFIRMED);
        createReservation(11, churrasqueira, ReservationStatus.PENDING);
        createReservation(12, academia, ReservationStatus.CONFIRMED);

        entityManager.flush();

        List<Reservation> result = reservationRepository.findReservationsForReport(
                5, 2026,
                List.of(ReservationStatus.CONFIRMED),
                List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA)
        );

        assertEquals(1, result.size());
        assertEquals(aprovadaSalao.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Keyset: retorna página ordenada por id e permite paginar após o último id")
    void findReservationsForReportPage_shouldReturnPagedKeyset() {
        createReservation(1, salao, ReservationStatus.CONFIRMED);
        createReservation(2, salao, ReservationStatus.CONFIRMED);
        createReservation(3, salao, ReservationStatus.CONFIRMED);

        entityManager.flush();

        UUID minUuid = new UUID(0L, 0L);
        List<Reservation> firstPage = reservationRepository.findReservationsForReportPage(
                5, 2026,
                List.of(ReservationStatus.CONFIRMED),
                List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA),
                minUuid,
                PageRequest.of(0, 2)
        );

        assertEquals(2, firstPage.size());
        UUID afterId = firstPage.get(firstPage.size() - 1).getId();

        List<Reservation> secondPage = reservationRepository.findReservationsForReportPage(
                5, 2026,
                List.of(ReservationStatus.CONFIRMED),
                List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA),
                afterId,
                PageRequest.of(0, 2)
        );

        assertEquals(1, secondPage.size());
        Set<UUID> firstPageIds = firstPage.stream().map(Reservation::getId).collect(Collectors.toSet());
        assertFalse(secondPage.stream().anyMatch(r -> firstPageIds.contains(r.getId())));
        Set<UUID> allIds = firstPageIds.stream()
                .collect(Collectors.toSet());
        secondPage.stream().map(Reservation::getId).forEach(allIds::add);
        assertEquals(3, allIds.size());
    }

    @Test
    @DisplayName("Keyset: retorna lista vazia quando não há reservas após o cursor")
    void findReservationsForReportPage_shouldReturnEmptyWhenAfterLast() {
        Reservation last = createReservation(15, churrasqueira, ReservationStatus.CONFIRMED);
        entityManager.flush();

        List<Reservation> result = reservationRepository.findReservationsForReportPage(
                5, 2026,
                List.of(ReservationStatus.CONFIRMED),
                List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA),
                last.getId(),
                PageRequest.of(0, 2)
        );

        assertTrue(result.isEmpty());
    }
}
