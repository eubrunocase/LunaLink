package com.LunaLink.application.core.ports.output;

import com.LunaLink.application.core.domain.Reservation;
import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.domain.Space;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryPort {


    Reservation save(Reservation reservation);
    void deleteById(Long id);
    List<Reservation> findAll();
    Optional<Reservation> findById(Long id);

    boolean existsByResidentAndDateAndSpace(Resident resident, LocalDate date, Space space);
    boolean existsByDateAndSpace(LocalDate date, Space space);
    boolean existsByDate(LocalDate date);
    Reservation findReservationById(Long id);

}
