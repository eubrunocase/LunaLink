package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.infrastructure.mapper.space.SpaceMapper;
import com.LunaLink.application.web.dto.SpaceDTO.SpaceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpaceMapperImpl implements SpaceMapper {

    @Override
    public SpaceResponseDTO toDTO(Space space) {
        if (space == null) {
            return null;
        }
        return new SpaceResponseDTO(space.getId(), space.getType());
    }

    @Override
    public List<SpaceResponseDTO> toDTOList(List<Space> spaces) {
        if (spaces == null) {
            return null;
        }
        return spaces.stream()
                .map(this::toDTO)
                .toList();
    }
}
