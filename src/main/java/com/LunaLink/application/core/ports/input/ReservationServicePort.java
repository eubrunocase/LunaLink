package com.LunaLink.application.core.ports.input;

import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;

import java.util.List;

public interface ReservationServicePort {

    ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequestDTO) throws Exception;
    List<ReservationResponseDTO> findAllReservations();
    ReservationResponseDTO findReservationById(Long id);
    //void deleteReservation(Long id, Long monthlyId);

}
