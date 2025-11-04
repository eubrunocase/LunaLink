package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.gym.CheckOut_Gym;

import java.util.List;
import java.util.UUID;

public interface CheckoutGymRepositoryPort {
    boolean existsByCheckInGym_Id(UUID checkIn_gymId);

    CheckOut_Gym findCheckOut_gymById(UUID id);
    List<CheckOut_Gym> findAll();
    CheckOut_Gym save(CheckOut_Gym checkOut_gym);
    void deleteById(UUID id);
}
