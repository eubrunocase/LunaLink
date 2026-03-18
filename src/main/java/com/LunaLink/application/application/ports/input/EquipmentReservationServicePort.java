package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EquipmentReservationServicePort {

    EquipmentReservationResponseDTO createReservation(EquipmentReservationRequestDTO dto, String userEmail);

    EquipmentReservationResponseDTO handoverEquipment(UUID id);

    EquipmentReservationResponseDTO returnEquipment(UUID id);

    List<EquipmentReservationResponseDTO> listReservations(LocalDate date, EquipmentReservationStatus status);
}
