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

    public List<OccurrenceResponseDTO> findAllOcurrences(){
        return occurrenceServicePort.findAll();
    }

    public void deleteOccurrence(String id) {
        occurrenceServicePort.deleteOccurrence(UUID.fromString(id));
    }

    public OccurrenceResponseDTO findOccurrenceById(UUID uuid) {
        return occurrenceServicePort.findById(uuid);
    }
}
