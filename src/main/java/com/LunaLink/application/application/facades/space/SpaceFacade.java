package com.LunaLink.application.application.facades.space;

import com.LunaLink.application.application.service.space.SpaceService;
import com.LunaLink.application.infrastructure.mapper.space.SpaceMapper;
import com.LunaLink.application.web.dto.SpaceDTO.SpaceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpaceFacade {

    private final SpaceService spaceService;
    private final SpaceMapper spaceMapper;

    public SpaceFacade(SpaceService spaceService, SpaceMapper spaceMapper) {
        this.spaceService = spaceService;
        this.spaceMapper = spaceMapper;
    }

    public List<SpaceResponseDTO> listAllSpaces() {
        return spaceMapper.toDTOList(spaceService.listAllReservations());
    }
}
