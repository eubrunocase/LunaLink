package com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para requisição de verificação de disponibilidade em período customizado
 *
 * Exemplo de requisição:
 * {
 *   "startDate": "2026-01-01",
 *   "endDate": "2026-01-31"
 * }
 */
public record AvailabilityPeriodRequestDTO(@JsonProperty("startDate") @NotNull LocalDate startDate,
                                           @JsonProperty("endDate") @NotNull LocalDate endDate
) {
}
