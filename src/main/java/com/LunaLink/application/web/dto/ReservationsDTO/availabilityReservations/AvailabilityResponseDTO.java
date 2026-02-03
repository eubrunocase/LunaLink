package com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para resposta simples de verificação de disponibilidade
 *
 * Exemplo de resposta:
 * {
 *   "spaceId": 1,
 *   "date": "2026-01-15",
 *   "available": true
 * }
 */
public record AvailabilityResponseDTO(@JsonProperty("spaceId") @NotNull Long spaceId,
                                      @JsonProperty("date") @NotNull LocalDate date,
                                      @JsonProperty("available") boolean available
) {
}
