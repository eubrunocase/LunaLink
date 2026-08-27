package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.ReservationsDTO.GuestResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GuestServicePort {

    List<GuestResponseDTO> getGuestsByReservation(UUID reservationId);

    void checkInGuest(UUID reservationId, UUID guestId);
}
