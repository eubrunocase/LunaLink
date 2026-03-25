package com.LunaLink.application.infrastructure.repository.occurrence;

import com.LunaLink.application.application.ports.output.OccurrenceRepositoryPort;
import com.LunaLink.application.domain.model.occurrence.Occurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OccurrenceRepository extends JpaRepository<Occurrence, UUID>, OccurrenceRepositoryPort {
    @Override
    Occurrence save(Occurrence occurrence);
    void deleteById(UUID id);
    Optional<Occurrence> findById(UUID id);

}
