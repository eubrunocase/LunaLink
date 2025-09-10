package com.LunaLink.application.web.mapper;

import com.LunaLink.application.core.domain.Reservation;
import com.LunaLink.application.core.infrastructure.persistence.reservation.ReservationMapper;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapperImpl implements ReservationMapper {

    @Override
    public ReservationResponseDTO toDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getDate(),
                mapResidentToDTO(reservation),
                mapSpaceToDTO(reservation)
        );
    }

    @Override
    public List<ReservationResponseDTO> toDtoLists(List<Reservation> reservations) {
        if (reservations == null) {
            return Collections.emptyList();
        }

        return reservations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Reservation toEntity(ReservationResponseDTO reservationResponseDTO) {
        if (reservationResponseDTO == null) {
            return null;
        }

        Reservation reservation = new Reservation();
        reservation.setDate(reservationResponseDTO.date());
        return reservation;
    }

    private ReservationResponseDTO.ResidentSummaryDTO mapResidentToDTO(Reservation reservation) {
        if (reservation.getResident() == null) {
            return null;
        }
        return new ReservationResponseDTO.ResidentSummaryDTO(
                reservation.getResident().getId(),
                reservation.getResident().getLogin()
        );
    }

    private ReservationResponseDTO.SpaceSummaryDTO mapSpaceToDTO(Reservation reservation) {
        if (reservation.getSpace() == null) {
            return null;
        }
        return new ReservationResponseDTO.SpaceSummaryDTO(
                reservation.getSpace().getId(),
                reservation.getSpace().getType().toString()
        );
    }

}
