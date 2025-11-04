package com.LunaLink.application.web.dto.checkOut_gym_DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

public record CheckOutCreateDTO(@JsonProperty("saida") @Valid LocalDateTime saida) {
}
