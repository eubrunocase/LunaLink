package com.LunaLink.application.web.dto.UserDTO;

import com.LunaLink.application.domain.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ResponseUserDTO(@JsonProperty("id") UUID id,
                              @JsonProperty("login") String login,
                              @JsonProperty("role") UserRoles role,
                              @JsonProperty("reservation") List<ReservationSummaryDTO> reservations) {

    public record ReservationSummaryDTO(
            @JsonProperty("id") UUID id,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("spaceType") String spaceType
    ) {}
}
