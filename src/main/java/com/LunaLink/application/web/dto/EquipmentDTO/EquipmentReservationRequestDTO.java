package com.LunaLink.application.web.dto.EquipmentDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record EquipmentReservationRequestDTO(
        @NotNull @JsonProperty("equipmentId") Long equipmentId,
        @NotNull @JsonProperty("date") LocalDate date,
        @NotNull @JsonProperty("startTime") LocalTime startTime,
        @NotNull @JsonProperty("endTime") LocalTime endTime
) {}
