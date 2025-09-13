package com.LunaLink.application.core.services.businnesRules.facades;

import com.LunaLink.application.core.ports.input.ReservationServicePort;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationServiceFacade {

    private final ReservationServicePort reservationService;

    public ReservationServiceFacade(ReservationServicePort reservationService) {
        this.reservationService = reservationService;
    }

    public ReservationResponseDTO createReservation (ReservationRequestDTO data) throws Exception {
        if (data == null ) {
            throw new Exception("Data is null");
        }

        return reservationService.createReservation(data);
    }

    public List<ReservationResponseDTO> findAllReservations () {
            List<ReservationResponseDTO> reservations = reservationService.findAllReservations();
            return reservations;
     }

    public ReservationResponseDTO findReservationById (Long id) {
        ReservationResponseDTO reservation = reservationService.findReservationById(id);
        return reservation;
    }

     public void deleteReservation (Long id) {
        reservationService.deleteReservation(id);
     }

     public ReservationResponseDTO updateReservation (Long id, ReservationRequestDTO reservationRequestDTO) {
        ReservationResponseDTO reservation = reservationService.updateReservation(id, reservationRequestDTO);
        return reservation;
     }

}
