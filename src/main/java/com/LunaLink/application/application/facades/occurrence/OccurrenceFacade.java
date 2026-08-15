package com.LunaLink.application.application.facades.occurrence;

import com.LunaLink.application.application.ports.input.OccurrenceServicePort;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OccurrenceFacade {

    private final OccurrenceServicePort occurrenceServicePort;

    public OccurrenceFacade(OccurrenceServicePort occurrenceServicePort) {
        this.occurrenceServicePort = occurrenceServicePort;
    }

    public OccurrenceResponseDTO createOccurrence(OccurrenceCreateRequestDTO dto, String userEmail) {
        return occurrenceServicePort.createOccurrence(dto, userEmail);
    }

    public List<OccurrenceResponseDTO> findAllOcurrences(String userEmail){
        return occurrenceServicePort.findAll(userEmail);
    }

    public void deleteOccurrence(UUID id, String userEmail) {
        occurrenceServicePort.deleteOccurrence(id, userEmail);
    }

    public OccurrenceResponseDTO findOccurrenceById(UUID uuid, String userEmail) {
        return occurrenceServicePort.findById(uuid, userEmail);
    }
}
