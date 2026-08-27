package com.LunaLink.application.application.service.liabilityterm;

import com.LunaLink.application.application.ports.output.LiabilityTermRepositoryPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.events.reservationEvents.ReservationConfirmedEvent;
import com.LunaLink.application.domain.model.reservation.LiabilityTerm;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiabilityTermServiceTest {

    @Mock
    private LiabilityTermRepositoryPort liabilityTermRepository;
    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private EventPublisher publisher;

    @InjectMocks
    private LiabilityTermService service;

    private Users resident;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        resident = new Users("Morador", "201", "morador@email.com", "pass", UserRoles.RESIDENT_ROLE);
        resident.setId(UUID.randomUUID());

        Space space = new Space();
        space.setId(1L);
        space.setType(SpaceType.SALAO_FESTAS);

        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setDate(LocalDate.now().plusDays(2));
        reservation.setStatus(ReservationStatus.AWAITING_SIGNATURE);
        reservation.setUser(resident);
        reservation.setSpace(space);
    }

    @Test
    @DisplayName("Deve assinar termo e mudar status para CONFIRMED")
    void signTerm_ShouldConfirm_WhenValidSignature() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(liabilityTermRepository.save(any(LiabilityTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        service.signTerm(reservation.getId(), resident.getId());

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verify(liabilityTermRepository, times(1)).save(any(LiabilityTerm.class));
    }

    @Test
    @DisplayName("Deve publicar ReservationConfirmedEvent após assinatura")
    void signTerm_ShouldPublishEvent_WhenValidSignature() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(liabilityTermRepository.save(any(LiabilityTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        service.signTerm(reservation.getId(), resident.getId());

        ArgumentCaptor<ReservationConfirmedEvent> captor = ArgumentCaptor.forClass(ReservationConfirmedEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());
        assertEquals(reservation.getId(), captor.getValue().getReservationId());
        assertEquals(resident.getId(), captor.getValue().getUserId());
    }

    @Test
    @DisplayName("Deve criar novo termo quando não existe")
    void signTerm_ShouldCreateNewTerm_WhenNotExists() {
        reservation.setLiabilityTerm(null);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(liabilityTermRepository.save(any(LiabilityTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        service.signTerm(reservation.getId(), resident.getId());

        verify(liabilityTermRepository, times(1)).save(any(LiabilityTerm.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando morador não é o responsável")
    void signTerm_ShouldThrow_WhenNotOwner() {
        UUID wrongUserId = UUID.randomUUID();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> service.signTerm(reservation.getId(), wrongUserId));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não está aguardando assinatura")
    void signTerm_ShouldThrow_WhenNotAwaitingSignature() {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> service.signTerm(reservation.getId(), resident.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando termo já foi assinado")
    void signTerm_ShouldThrow_WhenAlreadySigned() {
        LiabilityTerm signedTerm = new LiabilityTerm("Content", reservation);
        signedTerm.sign();
        reservation.setLiabilityTerm(signedTerm);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> service.signTerm(reservation.getId(), resident.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não é encontrada")
    void signTerm_ShouldThrow_WhenReservationNotFound() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.signTerm(UUID.randomUUID(), resident.getId()));
    }
}
