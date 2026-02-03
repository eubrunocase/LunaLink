package com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para resposta de disponibilidade em período customizado
 *
 * Exemplo de resposta:
 * {
 *   "spaceId": 1,
 *   "startDate": "2026-01-01",
 *   "endDate": "2026-01-31",
 *   "totalDaysInPeriod": 31,
 *   "unavailableDates": ["2026-01-05", "2026-01-10"],
 *   "availableDates": ["2026-01-01", "2026-01-02", ...],
 *   "fullyAvailable": false,
 *   "fullyBooked": false
 * }
 */
public record AvailabilityPeriodResponseDTO(@JsonProperty("spaceId") @NotNull Long spaceId,
                                            @JsonProperty("startDate") @NotNull LocalDate startDate,
                                            @JsonProperty("endDate") @NotNull LocalDate endDate,
                                            @JsonProperty("totalDaysInPeriod") Integer totalDaysInPeriod,
                                            @JsonProperty("unavailableDates") List<LocalDate> unavailableDates,
                                            @JsonProperty("availableDates") List<LocalDate> availableDates,
                                            @JsonProperty("fullyAvailable") boolean fullyAvailable,
                                            @JsonProperty("fullyBooked") boolean fullyBooked
) {
}
