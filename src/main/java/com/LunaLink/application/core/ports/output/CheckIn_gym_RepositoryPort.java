package com.LunaLink.application.core.ports.output;

import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.domain.Space;
import com.LunaLink.application.core.domain.gym.CheckIn_Gym;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CheckIn_gym_RepositoryPort {
    boolean existsByResidentAndEntrada(Resident resident, LocalDateTime entrada);


    CheckIn_Gym findCheckIn_gymById(UUID id);


    CheckIn_Gym save(CheckIn_Gym checkIn_gym);
    void deleteById(UUID id);
    List<CheckIn_Gym> findAll();
}
