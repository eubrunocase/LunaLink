package com.LunaLink.application.infrastructure.repository.reservation;

import com.LunaLink.application.application.ports.output.LiabilityTermRepositoryPort;
import com.LunaLink.application.domain.model.reservation.LiabilityTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiabilityTermRepository extends JpaRepository<LiabilityTerm, UUID>, LiabilityTermRepositoryPort {

    @Query("SELECT lt FROM LiabilityTerm lt WHERE lt.reservation.id = :reservationId")
    Optional<LiabilityTerm> findByReservationId(@Param("reservationId") UUID reservationId);
}
