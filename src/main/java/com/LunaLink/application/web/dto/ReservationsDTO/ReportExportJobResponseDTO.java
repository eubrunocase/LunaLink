package com.LunaLink.application.web.dto.ReservationsDTO;

import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.domain.enums.ReportExportStatus;

public record ReportExportJobResponseDTO(String jobId, ReportExportStatus status, String errorMessage) {

    public static ReportExportJobResponseDTO from(ReportExportJob job) {
        return new ReportExportJobResponseDTO(job.getId(), job.getStatus(), job.getErrorMessage());
    }
}
