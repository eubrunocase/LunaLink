package com.LunaLink.application.web.dto.ReservationsDTO;

import com.LunaLink.application.domain.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationRequestDTO(@JsonProperty("user") @Valid UUID userId,
                                    @JsonProperty("date") @Valid LocalDate date,
                                    @JsonProperty("space") @Valid Long spaceId) {

}
