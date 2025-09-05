package com.LunaLink.application.core.services.businnesRules;

import com.LunaLink.application.core.domain.Space;
import com.LunaLink.application.core.infrastructure.persistence.space.SpaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;

    public SpaceService(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    public List<Space> listAllReservations () {
        return spaceRepository.findAll();
    }

    public Optional<Space> findSpaceById (Long id) {
        return spaceRepository.findSpaceById(id);
    }

}
