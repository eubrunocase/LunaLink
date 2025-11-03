package com.LunaLink.application.core.infrastructure.persistence.space;

import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.enums.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    Optional<Space> findSpaceByType(SpaceType type);
    Optional<Space> findSpaceById(Long id);
}
