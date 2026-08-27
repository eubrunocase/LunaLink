package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ReservationCreateDTO(@JsonProperty("date")
                                   @Valid
                                   @NotNull(message = "Data é obrigatória")
                                   LocalDate date,

                                   @JsonProperty("space")
                                   @Valid
                                   @NotNull(message = "ID do espaço é obrigatório")
                                   Long spaceId,

                                   @JsonProperty("notes")
                                   String notes,

                                   @JsonProperty("guestList")
                                   List<String> guestList
) {
}
