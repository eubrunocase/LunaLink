package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.gym.CheckIn_Gym;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CheckinGymRepositoryPort {
    boolean existsByResidentAndEntrada(Resident resident, LocalDateTime entrada);


    CheckIn_Gym findCheckIn_gymById(UUID id);


    CheckIn_Gym save(CheckIn_Gym checkIn_gym);
    void deleteById(UUID id);
    List<CheckIn_Gym> findAll();
}
