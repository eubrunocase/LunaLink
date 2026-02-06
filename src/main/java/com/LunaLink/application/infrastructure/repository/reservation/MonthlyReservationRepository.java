package com.LunaLink.application.infrastructure.repository.reservation;

import com.LunaLink.application.domain.model.reservation.MonthlyReservations;
import com.LunaLink.application.domain.model.reservation.Reservation;
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
    void deleteMonthlyReservationsByReservation(Reservation reservation);
    void deleteById(long id);


    MonthlyReservations findByReservation(Reservation reservation);
}

