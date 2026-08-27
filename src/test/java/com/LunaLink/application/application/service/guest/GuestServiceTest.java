package com.LunaLink.application.application.service.guest;

import com.LunaLink.application.application.ports.output.GuestRepositoryPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.model.reservation.Guest;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.web.dto.ReservationsDTO.GuestResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepositoryPort guestRepository;
    @Mock
    private ReservationRepositoryPort reservationRepository;

    @InjectMocks
    private GuestService service;

    private Reservation reservation;
    private Guest guest;

    @BeforeEach
    void setUp() {
        Users resident = new Users("Morador", "201", "morador@email.com", "pass", UserRoles.RESIDENT_ROLE);
        resident.setId(UUID.randomUUID());

        Space space = new Space();
        space.setId(1L);
        space.setType(SpaceType.SALAO_FESTAS);

        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setDate(LocalDate.now().plusDays(2));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUser(resident);
        reservation.setSpace(space);

        guest = new Guest("Convidado 1", reservation);
    }

    @Test
    @DisplayName("Deve retornar lista de convidados da reserva")
    void getGuestsByReservation_ShouldReturnList_WhenReservationExists() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(guestRepository.findByReservationId(reservation.getId())).thenReturn(List.of(guest));

        List<GuestResponseDTO> result = service.getGuestsByReservation(reservation.getId());

        assertEquals(1, result.size());
        assertEquals("Convidado 1", result.get(0).name());
        assertFalse(result.get(0).checkedIn());
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não é encontrada (getGuests)")
    void getGuestsByReservation_ShouldThrow_WhenReservationNotFound() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getGuestsByReservation(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Deve realizar check-in do convidado no dia do evento")
    void checkInGuest_ShouldSucceed_WhenOnEventDate() {
        reservation.setDate(LocalDate.now());
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(guestRepository.findById(guest.getId())).thenReturn(Optional.of(guest));

        service.checkInGuest(reservation.getId(), guest.getId());

        assertTrue(guest.isCheckedIn());
        assertNotNull(guest.getCheckedInAt());
        verify(guestRepository, times(1)).save(guest);
    }

    @Test
    @DisplayName("Deve lançar exceção quando check-in é feito em dia diferente do evento")
    void checkInGuest_ShouldThrow_WhenNotOnEventDate() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> service.checkInGuest(reservation.getId(), guest.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando convidado já fez check-in")
    void checkInGuest_ShouldThrow_WhenAlreadyCheckedIn() {
        reservation.setDate(LocalDate.now());
        guest.checkIn();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(guestRepository.findById(guest.getId())).thenReturn(Optional.of(guest));

        assertThrows(IllegalStateException.class,
                () -> service.checkInGuest(reservation.getId(), guest.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando convidado não pertence à reserva")
    void checkInGuest_ShouldThrow_WhenGuestNotBelongsToReservation() {
        reservation.setDate(LocalDate.now());
        Reservation otherReservation = new Reservation();
        otherReservation.setId(UUID.randomUUID());
        Guest otherGuest = new Guest("Outro", otherReservation);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(guestRepository.findById(otherGuest.getId())).thenReturn(Optional.of(otherGuest));

        assertThrows(IllegalArgumentException.class,
                () -> service.checkInGuest(reservation.getId(), otherGuest.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando convidado não é encontrado")
    void checkInGuest_ShouldThrow_WhenGuestNotFound() {
        reservation.setDate(LocalDate.now());
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(guestRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.checkInGuest(reservation.getId(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não é encontrada (checkIn)")
    void checkInGuest_ShouldThrow_WhenReservationNotFound() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.checkInGuest(UUID.randomUUID(), UUID.randomUUID()));
    }
}
