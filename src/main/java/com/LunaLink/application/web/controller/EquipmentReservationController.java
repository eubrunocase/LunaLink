package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.equipment.EquipmentReservationFacade;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/equipment-reservation")
public class EquipmentReservationController {

    private final EquipmentReservationFacade facade;

    public EquipmentReservationController(EquipmentReservationFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<EquipmentReservationResponseDTO> createReservation(
            @RequestBody @Valid EquipmentReservationRequestDTO dto,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        EquipmentReservationResponseDTO response = facade.createReservation(dto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/handover")
    public ResponseEntity<EquipmentReservationResponseDTO> handoverEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok(facade.handoverEquipment(id));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<EquipmentReservationResponseDTO> returnEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok(facade.returnEquipment(id));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentReservationResponseDTO>> listReservations(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) EquipmentReservationStatus status) {
        try {
            List<EquipmentReservationResponseDTO> list = facade.listReservations(date, status);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("ERRO AO LISTAR RESERVAS DE EQUIPAMENTO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
