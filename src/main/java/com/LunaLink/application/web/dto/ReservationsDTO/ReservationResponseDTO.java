package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ReservationResponseDTO(@JsonProperty("id") Long id,
                                     @JsonProperty("date") LocalDate date,
                                     @JsonProperty("resident") ResidentSummaryDTO resident,
                                     @JsonProperty("space") SpaceSummaryDTO space) {


    public record ResidentSummaryDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("login") String login
    ) {}

    public record SpaceSummaryDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("type") String type
    ) {}

}
