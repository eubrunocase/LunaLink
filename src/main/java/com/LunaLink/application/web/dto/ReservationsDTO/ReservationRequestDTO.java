package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequestDTO(@JsonProperty("resident") @Valid Long residentId,
                                    @JsonProperty("date") @Valid LocalDate date,
                                    @JsonProperty("space") @Valid Long spaceId) {

}
