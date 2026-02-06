package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReservationServicePort {

    ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequestDTO) throws Exception;
    List<ReservationResponseDTO> findAllReservations();
    ReservationResponseDTO findReservationById(Long id);
    void deleteReservation(Long id);
    ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO reservationRequestDTO);
    Boolean checkAvaliability (LocalDate date, Long space, Long resident);

}
