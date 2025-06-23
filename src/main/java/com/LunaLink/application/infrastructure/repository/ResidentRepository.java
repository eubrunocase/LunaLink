package com.LunaLink.application.infrastructure.repository;

import com.LunaLink.application.core.Resident;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentRepository extends BaseRepository<Resident> {
    Resident findByLogin(String login);
}
