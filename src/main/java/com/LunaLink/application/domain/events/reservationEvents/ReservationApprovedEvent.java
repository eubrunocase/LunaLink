package com.LunaLink.application.domain.events.reservationEvents;

import com.LunaLink.application.domain.model.space.Space;

import java.time.LocalDate;
import java.util.UUID;

public class ReservationApprovedEvent {

    private final UUID reservationId;
    private final UUID userId;
    private final LocalDate date;
    private final Space space;

    public ReservationApprovedEvent(UUID reservationId, UUID userId, LocalDate date, Space space) {
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
