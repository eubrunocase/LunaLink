package com.example.LunaLink.controller;

import com.example.LunaLink.model.Reservation;
import com.example.LunaLink.repository.ReservationRepository;
import com.example.LunaLink.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService, ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Reservation createdReservation = reservationService.createReservation(reservation);
        return ResponseEntity.ok(createdReservation);
    }

    @GetMapping
     public List<Reservation> getAll () {
        return reservationRepository.findAll();
     }

     @GetMapping("/{id}")
     public Optional<Reservation> getById (@PathVariable long id) {
         return reservationRepository.findById(id);
     }

     @DeleteMapping("/{id}")
     public void DeleteById (@PathVariable long id) {
        reservationRepository.deleteById(id);
     }

     @DeleteMapping
     public void DeleteAll () {
        reservationRepository.deleteAll();
     }

}
