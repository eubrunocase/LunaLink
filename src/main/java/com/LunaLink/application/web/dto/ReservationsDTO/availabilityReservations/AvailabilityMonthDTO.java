package com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para resposta de disponibilidade de um mês inteiro
 *
 * Exemplo de resposta:
 * {
 *   "spaceId": 1,
 *   "year": 2026,
 *   "month": 1,
 *   "unavailableDates": ["2026-01-05", "2026-01-10", "2026-01-15"],
 *   "availableDates": ["2026-01-01", "2026-01-02", ...],
 *   "totalDays": 31,
 *   "unavailableCount": 3,
 *   "availableCount": 28,
 *   "occupancyPercentage": 9.68
 * }
 */
public record AvailabilityMonthDTO(@JsonProperty("spaceId") @NotNull Long spaceId,
                                   @JsonProperty("year") @NotNull Integer year,
                                   @JsonProperty("month") @NotNull Integer month,
                                   @JsonProperty("unavailableDates") List<LocalDate> unavailableDates,
                                   @JsonProperty("availableDates") List<LocalDate> availableDates,
                                   @JsonProperty("totalDays") Integer totalDays,
                                   @JsonProperty("unavailableCount") Integer unavailableCount,
                                   @JsonProperty("availableCount") Integer availableCount,
                                   @JsonProperty("occupancyPercentage") Double occupancyPercentage
) {
}
