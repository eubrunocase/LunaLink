package com.LunaLink.application.core.infrastructure.persistence.gym.checkIn;


import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.gym.CheckIn_Gym;
import com.LunaLink.application.application.ports.output.CheckIn_gym_RepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CheckIn_gym_repository extends JpaRepository<CheckIn_Gym, UUID>, CheckIn_gym_RepositoryPort {
    CheckIn_Gym findCheckIn_gymById(UUID id);

    boolean existsByResidentAndEntrada(Resident resident, LocalDateTime entrada);


}
