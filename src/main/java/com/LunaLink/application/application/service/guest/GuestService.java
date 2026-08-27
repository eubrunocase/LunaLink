package com.LunaLink.application.application.service.guest;

import com.LunaLink.application.application.ports.input.GuestServicePort;
import com.LunaLink.application.application.ports.output.GuestRepositoryPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.model.reservation.Guest;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.web.dto.ReservationsDTO.GuestResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GuestService implements GuestServicePort {

    private final GuestRepositoryPort guestRepository;
    private final ReservationRepositoryPort reservationRepository;

    public GuestService(GuestRepositoryPort guestRepository,
                        ReservationRepositoryPort reservationRepository) {
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<GuestResponseDTO> getGuestsByReservation(UUID reservationId) {
        reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));

        List<Guest> guests = guestRepository.findByReservationId(reservationId);
        return guests.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void checkInGuest(UUID reservationId, UUID guestId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));

        if (!reservation.getDate().equals(LocalDate.now())) {
            throw new IllegalStateException(
                    "Check-in só é permitido no dia do evento. Data da reserva: " + reservation.getDate());
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Convidado não encontrado."));

        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Convidado não pertence a esta reserva.");
        }

        if (guest.isCheckedIn()) {
            throw new IllegalStateException("Convidado já foi marcado como presente. Ação irreversível.");
        }

        guest.checkIn();
        guestRepository.save(guest);
    }

    private GuestResponseDTO toDTO(Guest guest) {
        return new GuestResponseDTO(
                guest.getId(),
                guest.getName(),
                guest.isCheckedIn(),
                guest.getCheckedInAt()
        );
    }
}
