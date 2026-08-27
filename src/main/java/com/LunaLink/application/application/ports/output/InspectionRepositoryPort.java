package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.model.inspection.SpaceInspection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InspectionRepositoryPort {

    SpaceInspection save(SpaceInspection inspection);

    Optional<SpaceInspection> findById(UUID id);

    List<SpaceInspection> findByReservationIdAndType(UUID reservationId, InspectionType type);

    List<SpaceInspection> findByReservationId(UUID reservationId);
}
