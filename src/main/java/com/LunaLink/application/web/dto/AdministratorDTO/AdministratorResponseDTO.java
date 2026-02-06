package com.LunaLink.application.web.dto.AdministratorDTO;

import com.LunaLink.application.domain.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record AdministratorResponseDTO(Long id,
                                       String login,
                                       UserRoles role) {

    public record ReservationSummaryDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("spaceType") String spaceType
    ) {}

}
