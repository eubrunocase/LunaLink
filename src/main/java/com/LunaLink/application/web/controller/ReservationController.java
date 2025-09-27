package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.facades.ReservationServiceFacade;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/lunaLink/reservation")
public class ReservationController {

    private final ReservationServiceFacade facade;

    public ReservationController(ReservationServiceFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createNewReservation (@RequestBody @Valid ReservationRequestDTO data) {
        try {
        ReservationResponseDTO reservationSaved = facade.createReservation(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationSaved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> listReservations () {
        return ResponseEntity.ok(facade.findAllReservations());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation (@PathVariable Long id) {
        facade.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findReservationById(@PathVariable Long id) {
        ReservationResponseDTO reservation = facade.findReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(@PathVariable Long id,
                                                                    @RequestBody ReservationRequestDTO reservationRequestDTO) {
        ReservationResponseDTO reservation = facade.updateReservation(id, reservationRequestDTO);
        return ResponseEntity.ok(reservation);
    }


}
