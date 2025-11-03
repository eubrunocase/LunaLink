package com.LunaLink.application.core.infrastructure.persistence.gym.checkOut;

import com.LunaLink.application.domain.model.gym.CheckOut_Gym;
import com.LunaLink.application.application.ports.output.CheckOutGym_RepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CheckOutGym_Repository extends JpaRepository<CheckOut_Gym, UUID>, CheckOutGym_RepositoryPort {
    CheckOut_Gym findCheckOut_gymById(UUID id);
    boolean existsByCheckInGym_Id(UUID checkIn_gym_id);
}
