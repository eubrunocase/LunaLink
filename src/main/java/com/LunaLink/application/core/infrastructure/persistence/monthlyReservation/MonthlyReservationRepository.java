package com.LunaLink.application.core.infrastructure.persistence.monthlyReservation;

import com.LunaLink.application.core.domain.MonthlyReservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface MonthlyReservationRepository extends JpaRepository<MonthlyReservations, Long> {
    MonthlyReservations findById(long id);

    @Modifying
    @Query("DELETE FROM MonthlyReservations m WHERE m.creationDate < ?1")
    void deleteByCreationDateBefore(LocalDateTime date);
}

