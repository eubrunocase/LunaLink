package com.LunaLink.application.web.dto.ReservationsDTO;

import com.LunaLink.application.core.Reservation;
import com.fasterxml.jackson.annotation.JsonProperty;


public record EditReservationDTO(@JsonProperty("reservation") Reservation savedReservation) {
}
