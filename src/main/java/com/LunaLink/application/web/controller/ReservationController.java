package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.businnesRules.ReservationService;
import com.LunaLink.application.core.Reservation;
import com.LunaLink.application.web.dto.ReservationRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/lunaLink/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> createNewReservation (@RequestBody ReservationRequest data) {
        try {
        Reservation reservationSaved = reservationService.createReservation(data.residentId(), data.date(), data.spaceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationSaved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<Reservation> listReservations () {
        return reservationService.findAllReservations();
    }





}
