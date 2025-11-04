package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.infrastructure.mapper.resident.ResidentMapper;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResidentMapperImpl implements ResidentMapper {


    @Override
    public ResidentResponseDTO toDTO(Resident resident) {
        if (resident == null) {
            return null;
        }

        List<ResidentResponseDTO.ReservationSummaryDTO> reservationDTOs =
                resident.getReservations().stream()
                        .map(reservation -> new ResidentResponseDTO.ReservationSummaryDTO(
                                reservation.getId(),
                                reservation.getDate(),
                                reservation.getSpace().getType().toString()
                        ))
                        .collect(Collectors.toList());

        return new ResidentResponseDTO(
                resident.getId(),
                resident.getLogin(),
                resident.getRole(),
                reservationDTOs
        );
    }

    @Override
    public List<ResidentResponseDTO> toDTOList(List<Resident> residents) {
        if (residents == null) {
            return null;
        }
        return residents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}

