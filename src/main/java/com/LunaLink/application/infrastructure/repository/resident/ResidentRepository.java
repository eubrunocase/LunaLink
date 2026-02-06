package com.LunaLink.application.infrastructure.repository.resident;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.application.ports.output.ResidentRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long>, ResidentRepositoryPort {
    Resident findByLogin(String login);
    Optional<Resident> findResidentByLogin(String login);
}
