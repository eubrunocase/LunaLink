package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonthlyRsDTO(@JsonProperty("resident") long residentId,
                           @JsonProperty("reservation") long reservationId) {
}
