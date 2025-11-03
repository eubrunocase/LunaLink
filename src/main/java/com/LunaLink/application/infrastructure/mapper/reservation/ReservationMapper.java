package com.LunaLink.application.core.infrastructure.persistence.reservation;

import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;

import java.util.List;

public interface ReservationMapper {

    ReservationResponseDTO toDto(Reservation reservation);
    List<ReservationResponseDTO> toDtoLists(List<Reservation> reservations);
    Reservation toEntity(ReservationResponseDTO reservationResponseDTO);
}
