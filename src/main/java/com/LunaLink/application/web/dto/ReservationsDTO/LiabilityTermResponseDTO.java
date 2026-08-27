package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LiabilityTermResponseDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("content") String content,
        @JsonProperty("signedByResident") boolean signedByResident,
        @JsonProperty("signedAt") LocalDateTime signedAt
) {
}
