package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.domain.model.equipment.EquipmentReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentReservationRepositoryPort {

    EquipmentReservation save(EquipmentReservation reservation);

    Optional<EquipmentReservation> findById(UUID id);

    List<EquipmentReservation> findAll();

    boolean hasConflict(
            Long equipmentId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            List<EquipmentReservationStatus> activeStatuses
    );

    List<EquipmentReservation> findAllByDate(LocalDate date);

    List<EquipmentReservation> findAllByStatus(EquipmentReservationStatus status);

    List<EquipmentReservation> findAllByDateAndStatus(LocalDate date, EquipmentReservationStatus status);
}
