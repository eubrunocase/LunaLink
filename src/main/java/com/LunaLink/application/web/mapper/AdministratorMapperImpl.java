package com.LunaLink.application.web.mapper;

import com.LunaLink.application.core.Administrator;
import com.LunaLink.application.core.Resident;
import com.LunaLink.application.infrastructure.repository.administrator.AdministratorMapper;
import com.LunaLink.application.web.dto.AdministratorDTO.AdmnistratorResponseDTO;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdministratorMapperImpl implements AdministratorMapper {

    @Override
    public AdmnistratorResponseDTO toDTO (Administrator administrator) {
        if (administrator == null) {
            return null;
        }

        List<AdmnistratorResponseDTO.ReservationSummaryDTO> reservationDTOs =
                administrator.getReservations().stream()
                        .map(reservation -> new AdmnistratorResponseDTO.ReservationSummaryDTO(
                                reservation.getId(),
                                reservation.getDate(),
                                reservation.getSpace().getType().toString()
                        ))
                        .collect(Collectors.toList());

        return new AdmnistratorResponseDTO(
                administrator.getId(),
                administrator.getLogin(),
                administrator.getRole(),
                reservationDTOs
        );
    }

    @Override
    public List<AdmnistratorResponseDTO> toDTOList(List<Administrator> administrators) {
        if (administrators == null) {
            return null;
        }
        return administrators.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
