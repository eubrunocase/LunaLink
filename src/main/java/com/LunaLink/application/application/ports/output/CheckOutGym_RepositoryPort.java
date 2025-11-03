package com.LunaLink.application.core.ports.output;

import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.domain.gym.CheckOut_Gym;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CheckOutGym_RepositoryPort {
    boolean existsByCheckInGym_Id(UUID checkIn_gymId);

    CheckOut_Gym findCheckOut_gymById(UUID id);
    List<CheckOut_Gym> findAll();
    CheckOut_Gym save(CheckOut_Gym checkOut_gym);
    void deleteById(UUID id);
}
