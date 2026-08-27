package com.LunaLink.application.infrastructure.repository.reservation;

import com.LunaLink.application.application.ports.output.GuestRepositoryPort;
import com.LunaLink.application.domain.model.reservation.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID>, GuestRepositoryPort {

    List<Guest> findByReservationId(UUID reservationId);

    @Query("SELECT COUNT(g) FROM Guest g WHERE g.reservation.id = :reservationId")
    long countByReservationId(@Param("reservationId") UUID reservationId);
}
