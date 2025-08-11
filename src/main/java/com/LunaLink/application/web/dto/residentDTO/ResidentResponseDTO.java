package com.LunaLink.application.web.dto.residentDTO;

import com.LunaLink.application.core.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;


public record ResidentResponseDTO(@JsonProperty("id") Long id,
                                  @JsonProperty("login") String login,
                                  @JsonProperty("role") UserRoles role,
                                  @JsonProperty("reservations") List<ReservationSummaryDTO> reservations) {

    public record ReservationSummaryDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("spaceType") String spaceType
    ) {}

}
