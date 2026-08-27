package com.LunaLink.application.web.dto.ReservationsDTO;

import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.enums.SpaceType;
import java.time.LocalDate;
import java.util.UUID;

public record InspectionPendingReservationDTO(
    UUID reservationId,
    LocalDate date,
    SpaceType spaceType,
    String spaceName,
    InspectionType pendingType,
    String residentName
) {}