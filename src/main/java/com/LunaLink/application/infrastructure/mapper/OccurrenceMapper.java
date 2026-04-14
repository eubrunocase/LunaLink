package com.LunaLink.application.infrastructure.mapper;

import com.LunaLink.application.domain.model.occurrence.Occurrence;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;

import java.util.List;

public interface OccurrenceMapper {
    OccurrenceResponseDTO toDto(Occurrence occurrence);
    List<OccurrenceResponseDTO> toDtoLists(List<Occurrence> occurrencesList);
    Occurrence toEntity(OccurrenceResponseDTO occurrenceResponseDTO);
}
