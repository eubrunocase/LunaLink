package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Space;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
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
