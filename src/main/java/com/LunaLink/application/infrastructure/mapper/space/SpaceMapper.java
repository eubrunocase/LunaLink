package com.LunaLink.application.infrastructure.mapper.space;

import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.web.dto.SpaceDTO.SpaceResponseDTO;

import java.util.List;

public interface SpaceMapper {
    SpaceResponseDTO toDTO(Space space);
    List<SpaceResponseDTO> toDTOList(List<Space> spaces);
}
