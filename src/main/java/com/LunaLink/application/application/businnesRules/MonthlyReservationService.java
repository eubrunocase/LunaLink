package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.MonthlyReservations;
import com.LunaLink.application.infrastructure.repository.monthlyReservation.MonthlyReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonthlyReservationService {

    private final MonthlyReservationRepository monthlyReservationRepository;

    public MonthlyReservationService(MonthlyReservationRepository monthlyReservationRepository) {
        this.monthlyReservationRepository = monthlyReservationRepository;
    }


    public List<MonthlyReservations> getMonthlyReservations() {
        return monthlyReservationRepository.findAll();
    }
}
