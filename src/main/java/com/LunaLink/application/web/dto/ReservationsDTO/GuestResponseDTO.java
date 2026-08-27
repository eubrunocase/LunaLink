package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record GuestResponseDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("checkedIn") boolean checkedIn,
        @JsonProperty("checkedInAt") LocalDateTime checkedInAt
) {
}
