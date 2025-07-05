package com.LunaLink.application.infrastructure.repository;

import com.LunaLink.application.core.Space;
import com.LunaLink.application.core.enums.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    Optional<Space> findSpaceByType(SpaceType type);
    Optional<Space> findSpaceById(Long id);
}
