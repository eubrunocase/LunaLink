package com.LunaLink.application.application.facades.equipment;

import com.LunaLink.application.application.ports.input.EquipmentReservationServicePort;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class EquipmentReservationFacade {

    private final EquipmentReservationServicePort servicePort;

    public EquipmentReservationFacade(EquipmentReservationServicePort servicePort) {
        this.servicePort = servicePort;
    }

    public EquipmentReservationResponseDTO createReservation(EquipmentReservationRequestDTO dto, String userEmail) {
        return servicePort.createReservation(dto, userEmail);
    }

    public EquipmentReservationResponseDTO handoverEquipment(UUID id) {
        return servicePort.handoverEquipment(id);
    }

    public EquipmentReservationResponseDTO returnEquipment(UUID id) {
        return servicePort.returnEquipment(id);
    }

    public EquipmentReservationResponseDTO cancelEquipmentReservation(UUID id, String userEmail) {
        return servicePort.cancelEquipmentReservation(id, userEmail);
    }

    public List<EquipmentReservationResponseDTO> listReservations(LocalDate date, EquipmentReservationStatus status) {
        return servicePort.listReservations(date, status);
    }

    public List<EquipmentReservationResponseDTO> listMyReservations(String userEmail) {
        return servicePort.listMyReservations(userEmail);
    }
}
