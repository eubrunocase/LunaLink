package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.administrator.Administrator;
import com.LunaLink.application.infrastructure.mapper.administrator.AdministratorMapper;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdministratorMapperImpl implements AdministratorMapper {

    @Override
    public AdministratorResponseDTO toDTO (Administrator administrator) {
        if (administrator == null) {
            return null;
        }

//        List<AdministratorResponseDTO.ReservationSummaryDTO> reservationDTOs =
//                administrator.getReservations().stream()
//                        .map(reservation -> new AdministratorResponseDTO.ReservationSummaryDTO(
//                                reservation.getId(),
//                                reservation.getDate(),
//                                reservation.getSpace().getType().toString()
//                        ))
//                        .collect(Collectors.toList());

        return new AdministratorResponseDTO(
                administrator.getId(),
                administrator.getLogin(),
                administrator.getRole()
                //reservationDTOs
        );
    }

    @Override
    public List<AdministratorResponseDTO> toDTOList(List<Administrator> administrators) {
        if (administrators == null) {
            return null;
        }
        return administrators.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
