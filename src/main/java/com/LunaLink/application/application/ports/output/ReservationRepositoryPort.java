package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.space.Space;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepositoryPort {


    Reservation save(Reservation reservation);
    void deleteById(Long id);
    List<Reservation> findAll();

    boolean existsByResidentAndDateAndSpace(Resident resident, LocalDate date, Space space);
    boolean existsByDateAndSpace(LocalDate date, Space space);
    boolean existsByDate(LocalDate date);
    Reservation findReservationById(Long id);

}
