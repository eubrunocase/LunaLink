package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.MonthlyReservations;
import com.LunaLink.application.infrastructure.repository.monthlyReservation.MonthlyReservationRepository;
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

}
