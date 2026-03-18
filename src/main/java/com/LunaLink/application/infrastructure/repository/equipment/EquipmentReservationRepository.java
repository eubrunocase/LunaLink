package com.LunaLink.application.infrastructure.repository.equipment;

import com.LunaLink.application.application.ports.output.EquipmentReservationRepositoryPort;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.domain.model.equipment.EquipmentReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentReservationRepository extends JpaRepository<EquipmentReservation, UUID>, EquipmentReservationRepositoryPort {

    @Override
    EquipmentReservation save(EquipmentReservation reservation);

    @Override
    Optional<EquipmentReservation> findById(UUID id);

    @Override
    List<EquipmentReservation> findAll();

    @Override
    @Query("""
        SELECT COUNT(r) > 0 FROM EquipmentReservation r
        WHERE r.equipment.id = :equipmentId
        AND r.date = :date
        AND r.status IN :activeStatuses
        AND (:startTime < r.endTime AND :endTime > r.startTime)
    """)
    boolean hasConflict(
            @Param("equipmentId") Long equipmentId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") List<EquipmentReservationStatus> activeStatuses
    );

    @Override
    List<EquipmentReservation> findAllByDate(LocalDate date);

    @Override
    List<EquipmentReservation> findAllByStatus(EquipmentReservationStatus status);

    @Override
    List<EquipmentReservation> findAllByDateAndStatus(LocalDate date, EquipmentReservationStatus status);
}
