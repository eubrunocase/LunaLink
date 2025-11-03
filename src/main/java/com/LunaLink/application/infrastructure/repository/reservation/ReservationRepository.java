package com.LunaLink.application.core.infrastructure.persistence.reservation;

import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryPort {
    boolean existsByResidentAndDateAndSpace(Resident resident, LocalDate date, Space space);
    boolean existsByDateAndSpace(LocalDate date, Space space);
    boolean existsByDate(LocalDate date);
    Reservation findReservationById(Long id);
}
