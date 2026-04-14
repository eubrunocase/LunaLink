package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.occurrence.Occurrence;
import com.LunaLink.application.infrastructure.mapper.OccurrenceMapper;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OccurrenceMapperImpl implements OccurrenceMapper {
    @Override
    public OccurrenceResponseDTO toDto(Occurrence occurrence) {
        return new OccurrenceResponseDTO(
                occurrence.getId(),
                occurrence.getUser().getName(),
                occurrence.getDescription(),
                occurrence.getIncidentDate(),
                occurrence.getCreatedAt()
                );
    }

    @Override
    public List<OccurrenceResponseDTO> toDtoLists(List<Occurrence> occurrencesList) {
            if (occurrencesList == null) {
                return null;
            }

            return occurrencesList.stream()
                    .map(this ::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    public Occurrence toEntity(OccurrenceResponseDTO occurrenceResponseDTO) {
        return null;
    }
}
