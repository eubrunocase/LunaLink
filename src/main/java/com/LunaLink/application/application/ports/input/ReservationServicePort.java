package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationServicePort {

    ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequestDTO) throws Exception;
    List<ReservationResponseDTO> findAllReservations();
    ReservationResponseDTO findReservationById(UUID id);
    void deleteReservation(UUID id);
    ReservationResponseDTO updateReservation(UUID id, ReservationRequestDTO reservationRequestDTO);
    Boolean checkAvaliability (LocalDate date, Long space, UUID user);


}
