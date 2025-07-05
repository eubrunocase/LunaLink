package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Space;
import com.LunaLink.application.core.enums.SpaceType;
import com.LunaLink.application.infrastructure.repository.SpaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;

    public SpaceService(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    public Space createSpace (SpaceType type) throws Exception{
        if (spaceRepository.findSpaceByType(type).isPresent()) {
            throw new Exception("Já existe um espaço cadastrado com esse tipo: " + type);
        }

        Space space = new Space();
        space.setType(type);
        return spaceRepository.save(space);
    }

    public List<Space> listAllReservations () {
        return spaceRepository.findAll();
    }

    public Optional<Space> findSpaceById (Long id) {
        return spaceRepository.findSpaceById(id);
    }

}
