package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionSubmitDTO;

import java.util.UUID;

public interface InspectionServicePort {

    void submitInspection(UUID reservationId, InspectionType type, InspectionSubmitDTO dto, UUID employeeId);
}
