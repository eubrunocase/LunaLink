package com.LunaLink.application.infrastructure.repository;

import com.LunaLink.application.core.Reservation;
import com.LunaLink.application.core.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReservationRepository extends BaseRepository<Reservation> {
    boolean existsByResidentAndDate(Resident resident, LocalDate date);
}
