package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.reservation.LiabilityTerm;

import java.util.Optional;
import java.util.UUID;

public interface LiabilityTermRepositoryPort {

    LiabilityTerm save(LiabilityTerm liabilityTerm);

    Optional<LiabilityTerm> findById(UUID id);

    Optional<LiabilityTerm> findByReservationId(UUID reservationId);
}
