package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationResponseDTO(@JsonProperty("id") UUID id,
                                     @JsonProperty("date") LocalDate date,
                                     @JsonProperty("user") UserSummaryDTO user,
                                     @JsonProperty("space") SpaceSummaryDTO space) {


    public record UserSummaryDTO(
            @JsonProperty("id") UUID id,
            @JsonProperty("login") String login
    ) {}

    public record SpaceSummaryDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("type") String type
    ) {}

}
