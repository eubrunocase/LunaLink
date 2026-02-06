package com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para estatísticas de ocupação de um espaço
 *
 * Exemplo de resposta:
 * {
 *   "spaceId": 1,
 *   "year": 2026,
 *   "month": 1,
 *   "totalDays": 31,
 *   "unavailableDays": 10,
 *   "availableDays": 21,
 *   "occupancyPercentage": 32.26
 * }
 */
public record OccupancyStatsResponseDTO(@JsonProperty("spaceId") @NotNull Long spaceId,
                                        @JsonProperty("year") @NotNull Integer year,
                                        @JsonProperty("month") @NotNull Integer month,
                                        @JsonProperty("totalDays") Integer totalDays,
                                        @JsonProperty("unavailableDays") Integer unavailableDays,
                                        @JsonProperty("availableDays") Integer availableDays,
                                        @JsonProperty("occupancyPercentage") Double occupancyPercentage
) {
}
