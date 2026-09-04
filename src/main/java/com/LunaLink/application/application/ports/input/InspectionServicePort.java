package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionSubmitDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InspectionServicePort {

    void submitInspection(UUID reservationId, InspectionType type, InspectionSubmitDTO dto, UUID employeeId);
    Map<String, String> generateUploadData(UUID userId, String fileName);
    List<Map<String, String>> generateDownloadUrls(UUID inspectionId);
    String generateDownloadUrl(UUID inspectionId, UUID itemId);
}
