package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.reservation.ReservationServiceFacade;
import com.LunaLink.application.application.ports.input.UserServicePort;
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
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/reservation")
public class ReservationController {

    private final ReservationServiceFacade facade;
    private final UserServicePort userServicePort;

    public ReservationController(ReservationServiceFacade facade, UserServicePort userServicePort) {
        this.facade = facade;
        this.userServicePort = userServicePort;
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
    public ResponseEntity<Void> deleteReservation (@PathVariable UUID id) {
        facade.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findReservationById(@PathVariable UUID id) {
        ReservationResponseDTO reservation = facade.findReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(@PathVariable UUID id,
                                                                    @RequestBody ReservationRequestDTO reservationRequestDTO) {
        ReservationResponseDTO reservation = facade.updateReservation(id, reservationRequestDTO);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/checkAvaliability/{date}/{spaceId}")
    public ResponseEntity<Boolean> checkAvaliability(@PathVariable LocalDate date,
                                                     Authentication authentication,
                                                     @PathVariable Long spaceId) {
        String login = authentication.getName();
        UUID residentId = userServicePort.findUserByLogin(login).id();

        Boolean checkAvaliability = facade.checkAvaliability(date, spaceId, residentId);

        if (checkAvaliability) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ReservationResponseDTO> approveReservation(@PathVariable UUID id) {
        ReservationResponseDTO response = facade.approveReservation(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ReservationResponseDTO> rejectReservation(@PathVariable UUID id) {
        ReservationResponseDTO response = facade.rejectReservation(id);
        return ResponseEntity.ok(response);
    }


}
