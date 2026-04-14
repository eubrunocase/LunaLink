package com.LunaLink.application.web.dto.OccurrenceDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceResponseDTO(
        UUID id,
        String userName,
        String description,
        LocalDateTime incidentDate,
        LocalDateTime createdAt
) {
}
