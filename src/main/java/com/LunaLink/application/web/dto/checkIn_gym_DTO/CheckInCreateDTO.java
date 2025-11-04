package com.LunaLink.application.web.dto.checkIn_gym_DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

public record CheckInCreateDTO(@JsonProperty("entrada") @Valid LocalDateTime entrada,
                               @JsonProperty("space") @Valid Long spaceId) {
}
