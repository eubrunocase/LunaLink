package com.LunaLink.application.domain.events;

import com.LunaLink.application.domain.model.space.Space;

import java.time.LocalDate;
import java.util.UUID;

public class ReservationRequestedEvent {

    private final UUID reservationId;
    private final UUID userId;
    private final LocalDate date;
    private final Space space;

    public ReservationRequestedEvent(UUID reservationId, UUID userId, LocalDate date, Space space) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.date = date;
        this.space = space;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Space getSpace() {
        return space;
    }


}
