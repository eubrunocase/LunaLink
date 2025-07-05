package com.LunaLink.application.infrastructure.repository;

import com.LunaLink.application.core.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Resident findByLogin(String login);
}
