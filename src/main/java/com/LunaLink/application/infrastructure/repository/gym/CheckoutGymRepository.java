package com.LunaLink.application.infrastructure.repository.gym;

import com.LunaLink.application.domain.model.gym.CheckOut_Gym;
import com.LunaLink.application.application.ports.output.CheckoutGymRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CheckoutGymRepository extends JpaRepository<CheckOut_Gym, UUID>, CheckoutGymRepositoryPort {
    CheckOut_Gym findCheckOut_gymById(UUID id);
    boolean existsByCheckInGym_Id(UUID checkIn_gym_id);
}
