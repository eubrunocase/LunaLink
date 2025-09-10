package com.LunaLink.application.core.services.businnesRules;

import com.LunaLink.application.core.domain.MonthlyReservations;
import com.LunaLink.application.core.domain.Reservation;
import com.LunaLink.application.core.infrastructure.persistence.monthlyReservation.MonthlyReservationRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyRsDTO;
import com.LunaLink.application.web.mapper.MonthlyReservationMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonthlyReservationService {

    private final MonthlyReservationRepository monthlyReservationRepository;
    private final MonthlyReservationMapper monthlyReservationMapper;

    public MonthlyReservationService(MonthlyReservationRepository monthlyReservationRepository,
                                     MonthlyReservationMapper monthlyReservationMapper) {
        this.monthlyReservationRepository = monthlyReservationRepository;
        this.monthlyReservationMapper = monthlyReservationMapper;
    }

    public MonthlyReservations findById (long id) {
        return monthlyReservationRepository.findById(id);
    }

    @Transactional
    public List<MonthlyRsDTO> getMonthlyReservationsDTO() {
        List<MonthlyReservations> monthlyReservations = monthlyReservationRepository.findAll();
        return monthlyReservationMapper.toDTOList(monthlyReservations);
    }

    public void deleteById (long id) {
        monthlyReservationRepository.deleteById(id);
    }

    public void deleteMonthlyReservation (Reservation reservation) {
        monthlyReservationRepository.deleteMonthlyReservationsByReservation(reservation);
    }
}
