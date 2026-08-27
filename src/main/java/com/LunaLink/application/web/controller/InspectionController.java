package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.ports.input.InspectionServicePort;
import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionSubmitDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/reservations")
public class InspectionController {

    private final InspectionServicePort inspectionServicePort;
    private final UserServicePort userServicePort;

    public InspectionController(InspectionServicePort inspectionServicePort,
                                UserServicePort userServicePort) {
        this.inspectionServicePort = inspectionServicePort;
        this.userServicePort = userServicePort;
    }

    @PostMapping("/{id}/inspection")
    public ResponseEntity<Void> submitInspection(
            @PathVariable UUID id,
            @RequestParam InspectionType type,
            @RequestBody @Valid InspectionSubmitDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        UUID employeeId = userServicePort.findUserByEmail(email).id();

        inspectionServicePort.submitInspection(id, type, dto, employeeId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
