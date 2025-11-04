package com.LunaLink.application.application.service.events;

public class monthlyReservationDeleteEvent {

    private final long monthlyReservationId;

    public monthlyReservationDeleteEvent(long monthlyReservationId) {
        this.monthlyReservationId = monthlyReservationId;
    }

    public long getMonthlyReservationId() {
        return monthlyReservationId;
    }
}
