package com.LunaLink.application.core.infrastructure.persistence.reservation;

import com.LunaLink.application.core.domain.Reservation;
import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.domain.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByResidentAndDateAndSpace(Resident resident, LocalDate date, Space space);
    boolean existsByDateAndSpace(LocalDate date, Space space);
    boolean existsByDate(LocalDate date);
    Reservation findReservationById(Long id);
}
