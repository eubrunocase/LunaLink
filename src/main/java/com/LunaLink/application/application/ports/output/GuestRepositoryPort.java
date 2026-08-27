package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.reservation.Guest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestRepositoryPort {

    Guest save(Guest guest);

    Optional<Guest> findById(UUID id);

    List<Guest> findByReservationId(UUID reservationId);

    long countByReservationId(UUID reservationId);
}
