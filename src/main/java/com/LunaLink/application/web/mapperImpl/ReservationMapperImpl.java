package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.infrastructure.mapper.reservation.ReservationMapper;
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
                mapSpaceToDTO(reservation),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getCanceledAt()
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

    private ReservationResponseDTO.UserSummaryDTO mapResidentToDTO(Reservation reservation) {
        if (reservation.getUser() == null) {
            return null;
        }
        return new ReservationResponseDTO.UserSummaryDTO(
                reservation.getUser().getId(),
                reservation.getUser().getLogin()
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
 