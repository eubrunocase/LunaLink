package com.LunaLink.application.web.dto.EquipmentDTO;

import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record EquipmentReservationResponseDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("equipmentName") String equipmentName,
        @JsonProperty("userName") String userName,
        @JsonProperty("userApartment") String userApartment,
        @JsonProperty("date") LocalDate date,
        @JsonProperty("startTime") LocalTime startTime,
        @JsonProperty("endTime") LocalTime endTime,
        @JsonProperty("status") EquipmentReservationStatus status,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("pickedUpAt") LocalDateTime pickedUpAt,
        @JsonProperty("returnedAt") LocalDateTime returnedAt
) {}
