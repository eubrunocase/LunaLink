package com.LunaLink.application.core.services.businnesRules.events;

public class monthlyReservationDeleteEvent {

    private final long monthlyReservationId;

    public monthlyReservationDeleteEvent(long monthlyReservationId) {
        this.monthlyReservationId = monthlyReservationId;
    }

    public long getMonthlyReservationId() {
        return monthlyReservationId;
    }
}
