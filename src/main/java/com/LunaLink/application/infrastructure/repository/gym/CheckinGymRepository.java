package com.LunaLink.application.infrastructure.repository.gym;


import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.gym.CheckIn_Gym;
import com.LunaLink.application.application.ports.output.CheckinGymRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CheckinGymRepository extends JpaRepository<CheckIn_Gym, UUID>, CheckinGymRepositoryPort {
    CheckIn_Gym findCheckIn_gymById(UUID id);

    boolean existsByResidentAndEntrada(Resident resident, LocalDateTime entrada);


}
