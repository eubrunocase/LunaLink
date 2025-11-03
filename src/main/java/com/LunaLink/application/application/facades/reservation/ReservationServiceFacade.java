package com.LunaLink.application.application.service.facades;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.ports.input.ResidentServicePort;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationServiceFacade {

    private final ReservationServicePort reservationService;
    private final ResidentServicePort residentService;

    public ReservationServiceFacade(ReservationServicePort reservationService, ResidentServicePort residentService) {
        this.reservationService = reservationService;
        this.residentService = residentService;
    }

    public ReservationResponseDTO createReservationForAuthenticatedUser (ReservationCreateDTO data, String login) throws Exception{
       if (data == null) {
           throw new Exception("Data is null");
       }

        Resident resident = residentService.findResidentByLogin(login);

        ReservationRequestDTO request = new ReservationRequestDTO(
                resident.getId(),
                data.date(),
                data.spaceId()
        );

        return reservationService.createReservation(request);
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
