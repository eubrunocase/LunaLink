package com.LunaLink.application.application.facades.reservation;

import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationServiceFacade {

    private final ReservationServicePort reservationService;
    private final UserServicePort userServicePort;

    public ReservationServiceFacade(ReservationServicePort reservationService, UserServicePort userServicePort) {
        this.reservationService = reservationService;
        this.userServicePort = userServicePort;
    }

    public ReservationResponseDTO createReservationForAuthenticatedUser (ReservationCreateDTO data, String email) {
       if (data == null) {
           return null;
       }

        ResponseUserDTO user = userServicePort.findUserByEmail(email);
        ReservationRequestDTO request = new ReservationRequestDTO(
                user.id(),
                data.date(),
                data.spaceId()
        );

        return reservationService.createReservation(request);
    }

    public List<ReservationResponseDTO> findAllReservations () {
            List<ReservationResponseDTO> reservations = reservationService.findAllReservations();
            return reservations;
     }

    public ReservationResponseDTO findReservationById (UUID id) {
        ReservationResponseDTO reservation = reservationService.findReservationById(id);
        return reservation;
    }

    public List<ReservationResponseDTO> findReservationsByUserId (UUID id) {
        return reservationService.findReservationsByUserId(id);
    }

     public void deleteReservation (UUID id) {
        reservationService.deleteReservation(id);
     }

     public ReservationResponseDTO updateReservation (UUID id, ReservationRequestDTO reservationRequestDTO) {
        ReservationResponseDTO reservation = reservationService.updateReservation(id, reservationRequestDTO);
        return reservation;
     }

    public ReservationResponseDTO approveReservation(UUID id) {
        return reservationService.approveReservation(id);
    }

    public ReservationResponseDTO rejectReservation(UUID id) {
        return reservationService.rejectReservation(id);
    }

     public Boolean checkAvaliability (LocalDate date, Long space, UUID user) {
        return reservationService.checkAvaliability(date, space, user);
     }

    public List<MonthlyReservationReportDTO> generateMonthlyReport(int month, int year) {
        return reservationService.generateMonthlyReport(month, year);
    }

    public ReportExportJobResponseDTO createMonthlyReportExport(int month, int year, ReportFormat format) {
        return reservationService.createMonthlyReportExport(month, year, format);
    }

    public ReportExportJobResponseDTO getMonthlyReportExportStatus(String jobId) {
        return reservationService.getMonthlyReportExportStatus(jobId);
    }

    public ReportExportJob getMonthlyReportExportFile(String jobId) {
        return reservationService.getMonthlyReportExportFile(jobId);
    }

}
