package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.reservation.ReservationServiceFacade;
import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionPendingReservationDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/reservation")
public class ReservationController {

    private final ReservationServiceFacade facade;
    private final UserServicePort userServicePort;

    public ReservationController(ReservationServiceFacade facade, UserServicePort userServicePort) {
        this.facade = facade;
        this.userServicePort = userServicePort;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createNewReservation (@RequestBody @Valid ReservationCreateDTO data,
                                                                        Authentication authentication) {
        String email = authentication.getName();
        System.out.println(data.toString());
        ReservationResponseDTO reservationSaved = facade.createReservationForAuthenticatedUser(data, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationSaved);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> listReservations () {
        return ResponseEntity.ok(facade.findAllReservations());
    }

    @GetMapping("/findByUser/{id}")
    public ResponseEntity<List<ReservationResponseDTO>> findByUserId(@PathVariable UUID id) {
        return ResponseEntity.ok(facade.findReservationsByUserId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation (@PathVariable UUID id) {
        facade.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending-inspection")
    @PreAuthorize("hasAnyRole('ADMIN_ROLE', 'EMPLOYEE')")
    public ResponseEntity<List<InspectionPendingReservationDTO>> getPendingInspection() {
        List<InspectionPendingReservationDTO> reservations =
            facade.findPendingInspectionReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findReservationById(@PathVariable UUID id) {
        ReservationResponseDTO reservation = facade.findReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(@PathVariable UUID id,
                                                                    @RequestBody ReservationRequestDTO reservationRequestDTO) {
        ReservationResponseDTO reservation = facade.updateReservation(id, reservationRequestDTO);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/checkAvaliability/{date}/{spaceId}")
    public ResponseEntity<Boolean> checkAvaliability(@PathVariable LocalDate date,
                                                     Authentication authentication,
                                                     @PathVariable Long spaceId) {
        String email = authentication.getName();
        UUID residentId = userServicePort.findUserByEmail(email).id();

        Boolean checkAvaliability = facade.checkAvaliability(date, spaceId, residentId);

        if (checkAvaliability) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ReservationResponseDTO> approveReservation(@PathVariable UUID id) {
        ReservationResponseDTO response = facade.approveReservation(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ReservationResponseDTO> rejectReservation(@PathVariable UUID id) {
        ReservationResponseDTO response = facade.rejectReservation(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/monthly")
    public ResponseEntity<List<MonthlyReservationReportDTO>> getMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        List<MonthlyReservationReportDTO> report = facade.generateMonthlyReport(month, year);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/report/monthly/export")
    public ResponseEntity<ReportExportJobResponseDTO> createMonthlyReportExport(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam ReportFormat format) {
        ReportExportJobResponseDTO response = facade.createMonthlyReportExport(month, year, format);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/report/monthly/export/{jobId}/status")
    public ResponseEntity<ReportExportJobResponseDTO> getMonthlyReportExportStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(facade.getMonthlyReportExportStatus(jobId));
    }

    @GetMapping("/report/monthly/export/{jobId}")
    public ResponseEntity<StreamingResponseBody> downloadMonthlyReportExport(@PathVariable String jobId) {
        ReportExportJob job = facade.getMonthlyReportExportFile(jobId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(job.getContentType()))
                .contentLength(job.getContentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + job.getFileName() + "\"")
                .body(output -> copy(job, output));
    }

    private void copy(ReportExportJob job, OutputStream output) {
        try (InputStream in = Files.newInputStream(job.getTempFile())) {
            in.transferTo(output);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao enviar o arquivo do relatório.", e);
        }
    }

}
