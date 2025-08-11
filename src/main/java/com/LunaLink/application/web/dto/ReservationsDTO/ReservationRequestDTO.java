package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;


public record ReservationRequestDTO(@JsonProperty("resident") Long residentId,
                                    @JsonProperty("date") LocalDate date,
                                    @JsonProperty("space") Long spaceId) {


}
