package com.LunaLink.application.core.infrastructure.persistence.reservation;

import com.LunaLink.application.core.domain.Reservation;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ReservationMapper {

    ReservationResponseDTO toDto(Reservation reservation);
    List<ReservationResponseDTO> toDtoLists(List<Reservation> reservations);
    Reservation toEntity(ReservationResponseDTO reservationResponseDTO);
}
