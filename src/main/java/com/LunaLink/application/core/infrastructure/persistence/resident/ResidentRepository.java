package com.LunaLink.application.core.infrastructure.persistence.resident;

import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.ports.output.ResidentRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long>, ResidentRepositoryPort {
    Resident findByLogin(String login);
}
