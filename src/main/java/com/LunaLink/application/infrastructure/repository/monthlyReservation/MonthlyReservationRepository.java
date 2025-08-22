package com.LunaLink.application.infrastructure.repository.monthlyReservation;

import com.LunaLink.application.core.MonthlyReservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyReservationRepository extends JpaRepository<MonthlyReservations, Long> {
    MonthlyReservations findById(long id);
}
