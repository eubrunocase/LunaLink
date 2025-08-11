package com.LunaLink.application.infrastructure.repository.resident;

import com.LunaLink.application.core.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Resident findByLogin(String login);
}
