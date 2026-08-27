package com.LunaLink.application.infrastructure.repository.inspection;

import com.LunaLink.application.application.ports.output.InspectionRepositoryPort;
import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.model.inspection.SpaceInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceInspectionRepository extends JpaRepository<SpaceInspection, UUID>, InspectionRepositoryPort {

    @Query("SELECT si FROM SpaceInspection si WHERE si.reservation.id = :reservationId AND si.type = :type")
    List<SpaceInspection> findByReservationIdAndType(
            @Param("reservationId") UUID reservationId,
            @Param("type") InspectionType type
    );

    @Query("SELECT si FROM SpaceInspection si WHERE si.reservation.id = :reservationId")
    List<SpaceInspection> findByReservationId(@Param("reservationId") UUID reservationId);
}
