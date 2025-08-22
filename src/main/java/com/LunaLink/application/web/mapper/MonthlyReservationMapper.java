package com.LunaLink.application.web.mapper;


import com.LunaLink.application.core.MonthlyReservations;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyRsDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MonthlyReservationMapper {

    public MonthlyRsDTO toDTO(MonthlyReservations monthlyReservations) {
        if (monthlyReservations == null) {
            return null;
        }

        return new MonthlyRsDTO(
                monthlyReservations.getResident().getId(),
                monthlyReservations.getReservation().getId()
        );
    }

    public List<MonthlyRsDTO> toDTOList(List<MonthlyReservations> monthlyReservations) {
        if (monthlyReservations == null) {
            return null;
        }

        return monthlyReservations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
