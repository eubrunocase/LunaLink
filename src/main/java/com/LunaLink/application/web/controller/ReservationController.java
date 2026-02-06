package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.reservation.ReservationServiceFacade;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.ports.input.ResidentServicePort;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/lunaLink/reservation")
public class ReservationController {

    private final ReservationServiceFacade facade;
    private final ReservationServicePort reservationServicePort;
    private final ResidentServicePort residentServicePort;

    public ReservationController(ReservationServiceFacade facade, ReservationServicePort reservationServicePort, ResidentServicePort residentServicePort) {
        this.facade = facade;
        this.reservationServicePort = reservationServicePort;
        this.residentServicePort = residentServicePort;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createNewReservation (@RequestBody @Valid ReservationCreateDTO data,
                                                                        Authentication authentication) {
        try {
            String login = authentication.getName();

        ReservationResponseDTO reservationSaved = facade.createReservationForAuthenticatedUser(data, login);
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

    @GetMapping("/checkAvaliability/{date}/{spaceId}")
    public ResponseEntity<Boolean> checkAvaliability(@PathVariable LocalDate date,
                                                     Authentication authentication,
                                                     @PathVariable Long spaceId) {
        String login = authentication.getName();
        Long residentId = residentServicePort.findResidentByLogin(login).getId();

        Boolean checkAvaliability = reservationServicePort.checkAvaliability(date, spaceId, residentId);

        if (checkAvaliability) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }


}
