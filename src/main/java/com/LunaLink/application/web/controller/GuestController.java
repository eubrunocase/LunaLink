package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.ports.input.GuestServicePort;
import com.LunaLink.application.web.dto.ReservationsDTO.GuestResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/reservations")
public class GuestController {

    private final GuestServicePort guestServicePort;

    public GuestController(GuestServicePort guestServicePort) {
        this.guestServicePort = guestServicePort;
    }

    @GetMapping("/{id}/guests")
    public ResponseEntity<List<GuestResponseDTO>> getGuests(@PathVariable UUID id) {
        List<GuestResponseDTO> guests = guestServicePort.getGuestsByReservation(id);
        return ResponseEntity.ok(guests);
    }

    @PatchMapping("/{id}/guests/{guestId}/check-in")
    public ResponseEntity<Void> checkInGuest(
            @PathVariable UUID id,
            @PathVariable UUID guestId) {
        guestServicePort.checkInGuest(id, guestId);
        return ResponseEntity.ok().build();
    }
}
