package com.LunaLink.application.web.dto.checkIn_gym_DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckIn_Gym_ResponseDTO(@JsonProperty("id") UUID id,
                                      @JsonProperty("resident") ResidentSummaryDTO resident,
                                      @JsonProperty("entrada") LocalDateTime entrada,
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
