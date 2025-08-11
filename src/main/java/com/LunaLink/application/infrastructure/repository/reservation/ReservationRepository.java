package com.LunaLink.application.infrastructure.repository.reservation;

import com.LunaLink.application.core.Reservation;
import com.LunaLink.application.core.Resident;
import com.LunaLink.application.core.Space;
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
