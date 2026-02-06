package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.service.reservation.MonthlyReservationService;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyRsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/reservaMensal")
public class MonthlyReservationController {

    private final MonthlyReservationService monthlyReservationService;

    public MonthlyReservationController(MonthlyReservationService monthlyReservationService) {
        this.monthlyReservationService = monthlyReservationService;
    }

    @GetMapping
    public List<MonthlyRsDTO> getMonthlyReservations() {
        return monthlyReservationService.getMonthlyReservationsDTO();
    }

}
