package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.application.service.report.ReportExportService;
import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.reservationEvents.ReservationApprovedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRejectedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.reservation.ReservationMapper;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private SpaceRepository spaceRepository;
    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private EventPublisher publisher;
    @Mock
    private ReportExportService reportExportService;

    @InjectMocks
    private ReservationService service;

    private Users user;
    private Space space;
    private Reservation reservation;
    private ReservationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = new Users("User", "101", "testUser@email.com", "pass", UserRoles.RESIDENT_ROLE);
        user.setId(UUID.randomUUID());

        space = new Space();
        space.setId(1L);
        space.setType(SpaceType.SALAO_FESTAS);

        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setDate(LocalDate.now().plusDays(1));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setUser(user);
        reservation.setSpace(space);

        requestDTO = new ReservationRequestDTO(user.getId(), LocalDate.now().plusDays(1), space.getId());
    }

    @Test
    @DisplayName("Deve criar reserva com sucesso quando data disponível")
    void createReservation_ShouldSucceed_WhenDateIsAvailable() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findSpaceById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.existsByDateAndStatusIn(any(), anyList())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        
        ReservationResponseDTO expectedResponse = new ReservationResponseDTO(
                reservation.getId(), reservation.getDate(), null, null, 
                ReservationStatus.PENDING, LocalDateTime.now(), null
        );
        when(reservationMapper.toDto(any(Reservation.class))).thenReturn(expectedResponse);

        // Act
        ReservationResponseDTO result = service.createReservation(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.PENDING, result.status());
        verify(publisher, times(1)).publishEvent(any(ReservationRequestedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar reserva em data indisponível")
    void createReservation_ShouldThrowException_WhenDateUnavailable() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findSpaceById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.existsByDateAndStatusIn(any(), anyList())).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.createReservation(requestDTO));
            
        assertTrue(exception.getMessage().contains("Data indisponível"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve aprovar reserva pendente com sucesso")
    void approveReservation_ShouldUpdateStatus_WhenPending() {
        // Arrange
        UUID reservationId = reservation.getId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        
        ReservationResponseDTO expectedResponse = new ReservationResponseDTO(
                reservationId, reservation.getDate(), null, null, 
                ReservationStatus.APPROVED, LocalDateTime.now(), null
        );

        // Act
        ReservationResponseDTO result = service.approveReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.APPROVED, reservation.getStatus());
        verify(publisher, times(1)).publishEvent(any(ReservationApprovedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar aprovar reserva não pendente")
    void approveReservation_ShouldThrowException_WhenNotPending() {
        // Arrange
        reservation.setStatus(ReservationStatus.REJECTED);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.approveReservation(reservation.getId()));
            
        assertEquals("Apenas reservas com status PENDENTE podem ser aprovadas.", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar reserva pendente com sucesso")
    void rejectReservation_ShouldUpdateStatus_WhenPending() {
        // Arrange
        UUID reservationId = reservation.getId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        service.rejectReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.REJECTED, reservation.getStatus());
        verify(publisher, times(1)).publishEvent(any(ReservationRejectedEvent.class));
    }
    
    @Test
    @DisplayName("Deve verificar disponibilidade corretamente")
    void checkAvailability_ShouldReturnTrue_WhenAvailable() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findSpaceById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.existsByDateAndStatusIn(any(), anyList())).thenReturn(false);

        // Act
        Boolean isAvailable = service.checkAvaliability(LocalDate.now(), space.getId(), user.getId());

        // Assert
        assertTrue(isAvailable);
    }

    @Test
    @DisplayName("Deve gerar relatório mensal corretamente")
    void generateMonthlyReport_ShouldReturnList_WhenFound() {
        // Arrange
        int month = 5;
        int year = 2026;
        when(reservationRepository.findReservationsForReport(eq(month), eq(year), anyList(), anyList()))
                .thenReturn(List.of(reservation));

        // Act
        List<MonthlyReservationReportDTO> report = service.generateMonthlyReport(month, year);

        // Assert
        assertNotNull(report);
        assertEquals(1, report.size());
        assertEquals("User", report.get(0).residentName());
        assertEquals("101", report.get(0).apartment());
        assertEquals("SALAO_FESTAS", report.get(0).spaceType());
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar relatório com mês inválido")
    void generateMonthlyReport_ShouldThrowException_WhenInvalidMonth() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(0, 2026));
        assertTrue(exception.getMessage().contains("Mês inválido"));
        verify(reservationRepository, never()).findReservationsForReport(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar relatório com ano inválido")
    void generateMonthlyReport_ShouldThrowException_WhenInvalidYear() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(5, 2019));
        assertTrue(exception.getMessage().contains("Ano inválido"));
        verify(reservationRepository, never()).findReservationsForReport(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver reservas no período")
    void generateMonthlyReport_ShouldReturnEmptyList_WhenNoReservations() {
        when(reservationRepository.findReservationsForReport(eq(5), eq(2026), anyList(), anyList()))
                .thenReturn(List.of());

        List<MonthlyReservationReportDTO> report = service.generateMonthlyReport(5, 2026);

        assertNotNull(report);
        assertTrue(report.isEmpty());
    }

    @Test
    @DisplayName("Deve filtrar relatório por status aprovado e espaços tarifados")
    void generateMonthlyReport_ShouldFilterByApprovedStatusAndBillableSpaces() {
        service.generateMonthlyReport(5, 2026);

        verify(reservationRepository).findReservationsForReport(
                eq(5),
                eq(2026),
                eq(List.of(ReservationStatus.APPROVED)),
                eq(List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA))
        );
    }

    @Test
    @DisplayName("Deve criar job de exportação e disparar geração assíncrona")
    void createMonthlyReportExport_ShouldCreateJobAndTriggerGeneration() {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.PDF);
        when(reportExportService.createJob(5, 2026, ReportFormat.PDF)).thenReturn(job);

        ReportExportJobResponseDTO response = service.createMonthlyReportExport(5, 2026, ReportFormat.PDF);

        assertNotNull(response);
        assertEquals(job.getId(), response.jobId());
        assertEquals(ReportExportStatus.PROCESSING, response.status());
        verify(reportExportService).generate(job);
    }

    @Test
    @DisplayName("Deve consultar o status de um job de exportação")
    void getMonthlyReportExportStatus_ShouldReturnJobStatus() {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.DOCX).markCompleted();
        when(reportExportService.getJob(job.getId())).thenReturn(job);

        ReportExportJobResponseDTO response = service.getMonthlyReportExportStatus(job.getId());

        assertEquals(job.getId(), response.jobId());
        assertEquals(ReportExportStatus.READY, response.status());
    }

    @Test
    @DisplayName("Deve retornar o job pronto para download")
    void getMonthlyReportExportFile_ShouldReturnReadyJob() {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.PDF).markCompleted();
        job.setTempFile(Path.of("/tmp/relatorio.pdf"));
        when(reportExportService.getReadyJob(job.getId())).thenReturn(job);

        ReportExportJob result = service.getMonthlyReportExportFile(job.getId());

        assertEquals(job, result);
    }
}
