package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionPendingReservationDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservationServicePort {

    ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequestDTO);
    List<ReservationResponseDTO> findAllReservations();
    ReservationResponseDTO findReservationById(UUID id);
    void deleteReservation(UUID id);
    ReservationResponseDTO updateReservation(UUID id, ReservationRequestDTO reservationRequestDTO);
    Boolean checkAvaliability (LocalDate date, Long space, UUID user);

    ReservationResponseDTO approveReservation(UUID id);
    ReservationResponseDTO rejectReservation(UUID id);

    List<MonthlyReservationReportDTO> generateMonthlyReport(int month, int year);

    ReportExportJobResponseDTO createMonthlyReportExport(int month, int year, ReportFormat format);
    ReportExportJobResponseDTO getMonthlyReportExportStatus(String jobId);
    ReportExportJob getMonthlyReportExportFile(String jobId);

    List<ReservationResponseDTO> findReservationsByUserId(UUID userId);

    List<InspectionPendingReservationDTO> findPendingInspectionReservations();

}
