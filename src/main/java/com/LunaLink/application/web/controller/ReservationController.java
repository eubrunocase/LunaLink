package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.ReservationService;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createNewReservation (@RequestBody ReservationRequestDTO data) {
        try {
        ReservationResponseDTO reservationSaved = reservationService.createReservation(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationSaved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> listReservations () {
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @DeleteMapping("/{id}")
    public void deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }


}
