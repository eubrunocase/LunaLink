package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OccurrenceServicePort {
    OccurrenceResponseDTO createOccurrence(OccurrenceCreateRequestDTO dto, String userEmail);
    void deleteOccurrence(UUID uuid);
    OccurrenceResponseDTO findById(UUID uuid);
    List<OccurrenceResponseDTO> findAll();
}
