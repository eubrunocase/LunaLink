package com.LunaLink.application.web.dto.OccurrenceDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record OccurrenceCreateRequestDTO(
        @NotBlank(message = "A descrição da ocorrência não pode estar vazia")
        String description,
        
        @NotNull(message = "A data do incidente é obrigatória")
        LocalDateTime incidentDate
) {
}
