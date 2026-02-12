package com.LunaLink.application.application.facades.reservation;

import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationServiceFacade {

    private final ReservationServicePort reservationService;
    private final UserServicePort userServicePort;

    public ReservationServiceFacade(ReservationServicePort reservationService, UserServicePort userServicePort) {
        this.reservationService = reservationService;
        this.userServicePort = userServicePort;
    }

    public ReservationResponseDTO createReservationForAuthenticatedUser (ReservationCreateDTO data, String login) throws Exception{
       if (data == null) {
           throw new Exception("Data is null");
       }

        ResponseUserDTO user = userServicePort.findUserByLogin(login);
        ReservationRequestDTO request = new ReservationRequestDTO(
                user.id(),
                data.date(),
                data.spaceId()
        );

        return reservationService.createReservation(request);
    }

    public List<ReservationResponseDTO> findAllReservations () {
            List<ReservationResponseDTO> reservations = reservationService.findAllReservations();
            return reservations;
     }

    public ReservationResponseDTO findReservationById (UUID id) {
        ReservationResponseDTO reservation = reservationService.findReservationById(id);
        return reservation;
    }

     public void deleteReservation (UUID id) {
        reservationService.deleteReservation(id);
     }

     public ReservationResponseDTO updateReservation (UUID id, ReservationRequestDTO reservationRequestDTO) {
        ReservationResponseDTO reservation = reservationService.updateReservation(id, reservationRequestDTO);
        return reservation;
     }


     public Boolean checkAvaliability (LocalDate date, Long space, UUID user) {
        return reservationService.checkAvaliability(date, space, user);
     }

}
